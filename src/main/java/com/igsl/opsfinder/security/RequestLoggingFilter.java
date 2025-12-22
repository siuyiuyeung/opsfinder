package com.igsl.opsfinder.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to log HTTP requests with authenticated username and request path.
 * Runs after JwtAuthenticationFilter to access authenticated user information.
 * Uses SLF4J MDC (Mapped Diagnostic Context) to add username to all log entries
 * within the request lifecycle, providing complete audit trail across all application layers.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip logging for health check endpoint to reduce log noise
        String path = request.getRequestURI();
        if (path.equals("/actuator/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract username from SecurityContext (populated by JwtAuthenticationFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = getUsername(authentication);

        // Add username to MDC for thread-local logging context
        MDC.put("username", username);

        // Log request
        String method = request.getMethod();
        long startTime = System.currentTimeMillis();

        logger.info("[REQUEST] method={} path={}", method, path);

        try {
            // Continue filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Log response with duration and status code
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            logger.info("[RESPONSE] method={} path={} status={} duration={}ms",
                       method, path, status, duration);

            // Clear MDC to prevent memory leaks in thread pools
            MDC.clear();
        }
    }

    /**
     * Extract username from authentication, handling anonymous users.
     *
     * @param authentication Spring Security authentication object
     * @return username if authenticated, "anonymous" otherwise
     */
    private String getUsername(Authentication authentication) {
        if (authentication != null &&
            authentication.isAuthenticated() &&
            !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
