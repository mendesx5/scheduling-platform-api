package com.mendes.scheduling_platform.security;

import com.mendes.scheduling_platform.tenant.Tenant;
import com.mendes.scheduling_platform.tenant.TenantRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final TenantRepository tenants;

    public JwtAuthenticationFilter(JwtService jwt, TenantRepository tenants) {
        this.jwt = jwt;
        this.tenants = tenants;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                authenticate(header.substring(7), response);
                if (response.isCommitted()) return;
            }
            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou expirado");
        } finally {
            TenantContext.clear();
        }
    }

    private void authenticate(String token, HttpServletResponse response) throws IOException {
        Claims claims = jwt.parse(token);
        boolean platform = Boolean.TRUE.equals(claims.get("platform", Boolean.class));
        Long tenantId = claims.get("tenantId", Long.class);
        String authority;

        if (platform) {
            authority = "ROLE_PLATFORM_ADMIN";
        } else {
            if (tenantId == null || tenants.findById(tenantId)
                    .map(Tenant::getStatus)
                    .filter(Tenant.TenantStatus.ACTIVE::equals)
                    .isEmpty()) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant suspenso ou inexistente");
                return;
            }
            TenantContext.set(tenantId);
            authority = "ROLE_" + claims.get("role", String.class);
        }

        var authentication = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null, List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
