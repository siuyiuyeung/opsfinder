package com.igsl.opsfinder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS configuration properties.
 * Binds to 'cors.*' properties in application.yml.
 */
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    /**
     * List of allowed origins (e.g., http://localhost:3000, https://your-domain.com).
     * Can be set via ALLOWED_ORIGINS environment variable.
     */
    private List<String> allowedOrigins = new ArrayList<>();

    /**
     * List of allowed HTTP methods (e.g., GET, POST, PUT, DELETE).
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");

    /**
     * List of allowed headers (e.g., Authorization, Content-Type).
     */
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "X-Requested-With");

    /**
     * List of headers to expose to the client.
     */
    private List<String> exposedHeaders = List.of("Authorization");

    /**
     * Whether credentials (cookies, authorization headers) are allowed.
     */
    private boolean allowCredentials = true;

    /**
     * Maximum age (in seconds) for caching preflight requests.
     */
    private Long maxAge = 3600L;

    // Getters and Setters

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public Long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }
}
