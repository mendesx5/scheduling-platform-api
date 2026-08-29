package com.mendes.scheduling_platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int authLimit;
    private final int bookingLimit;
    private final long windowSeconds;
    private final boolean trustForwardedFor;

    public RateLimitFilter(@Value("${app.rate-limit.auth-per-minute:10}") int authLimit,
                           @Value("${app.rate-limit.bookings-per-minute:20}") int bookingLimit,
                           @Value("${app.rate-limit.window-seconds:60}") long windowSeconds,
                           @Value("${app.rate-limit.trust-forwarded-for:false}") boolean trustForwardedFor) {
        this.authLimit = authLimit;
        this.bookingLimit = bookingLimit;
        this.windowSeconds = windowSeconds;
        this.trustForwardedFor = trustForwardedFor;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod())) return true;
        String uri = request.getRequestURI();
        return !(uri.equals("/auth/login") || uri.equals("/platform/auth/login")
                || uri.matches("/public/[^/]+/bookings"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        int limit = uri.endsWith("/bookings") ? bookingLimit : authLimit;
        String key = clientIp(request) + ':' + uri;
        long now = Instant.now().getEpochSecond();
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAt >= windowSeconds) return new Window(now, 1);
            return new Window(current.startedAt, current.requests + 1);
        });
        if (window.requests > limit) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Muitas solicitações. Tente novamente em instantes.\"}");
            return;
        }
        if (windows.size() > 10_000) windows.entrySet().removeIf(e -> now - e.getValue().startedAt >= windowSeconds);
        chain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = trustForwardedFor ? request.getHeader("X-Forwarded-For") : null;
        return forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr() : forwarded.split(",", 2)[0].trim();
    }

    private record Window(long startedAt, int requests) {}
}
