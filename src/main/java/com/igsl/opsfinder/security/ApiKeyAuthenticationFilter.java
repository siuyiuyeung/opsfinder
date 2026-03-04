package com.igsl.opsfinder.security;

import com.igsl.opsfinder.entity.ApiKey;
import com.igsl.opsfinder.exception.RateLimitExceededException;
import com.igsl.opsfinder.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Filter that authenticates requests carrying an X-API-Key header.
 * If the header is absent the filter is a no-op and the JWT filter handles the request.
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String rawKey = request.getHeader(API_KEY_HEADER);
        if (!StringUtils.hasText(rawKey)) {
            // No X-API-Key header — let JWT filter handle it
            filterChain.doFilter(request, response);
            return;
        }

        String keyHash = apiKeyService.hashKey(rawKey);
        Optional<ApiKey> optKey = apiKeyService.findAndValidateApiKey(keyHash);

        if (optKey.isEmpty()) {
            sendUnauthorized(response, "Invalid API key");
            return;
        }

        ApiKey apiKey = optKey.get();

        if (!Boolean.TRUE.equals(apiKey.getActive())) {
            sendUnauthorized(response, "API key has been revoked");
            return;
        }

        if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(LocalDateTime.now())) {
            sendUnauthorized(response, "API key has expired");
            return;
        }

        try {
            apiKeyService.checkRateLimit(apiKey);
        } catch (RateLimitExceededException e) {
            response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT); // 429
            response.setStatus(429);
            response.addHeader("Retry-After", "3600");
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"" + e.getMessage() + "\"}");
            return;
        }

        // Authenticate as the key's owner
        UserDetails userDetails = userDetailsService.loadUserByUsername(apiKey.getUser().getUsername());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            String endpoint = request.getRequestURI();
            String method = request.getMethod();
            String clientIp = getClientIp(request);

            // Fire-and-forget async calls
            apiKeyService.logUsage(apiKey.getId(), endpoint, method, clientIp, status, elapsed);
            apiKeyService.updateLastUsed(apiKey.getId());
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
