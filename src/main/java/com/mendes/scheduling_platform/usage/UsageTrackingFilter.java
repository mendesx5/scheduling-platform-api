package com.mendes.scheduling_platform.usage;
import com.mendes.scheduling_platform.security.TenantContext; import jakarta.servlet.*; import jakarta.servlet.http.HttpServletRequest; import org.springframework.core.Ordered; import org.springframework.core.annotation.Order; import org.springframework.stereotype.Component; import java.io.IOException;
@Component @Order(Ordered.LOWEST_PRECEDENCE)
public class UsageTrackingFilter implements Filter {
 private final UsageTracker tracker; public UsageTrackingFilter(UsageTracker t){tracker=t;}
 @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{try{if(req instanceof HttpServletRequest r && !r.getRequestURI().startsWith("/public/"))tracker.request();chain.doFilter(req,res);}finally{TenantContext.clear();}}
}
