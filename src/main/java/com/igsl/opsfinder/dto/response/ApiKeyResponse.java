package com.igsl.opsfinder.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for an API key (no plaintext key).
 */
@Data
public class ApiKeyResponse {

    private Long id;
    private String name;
    private String description;
    private String keyPrefix;
    private Long userId;
    private String username;
    private Boolean active;
    private LocalDateTime expiresAt;
    private Integer rateLimitPerHour;
    private LocalDateTime lastUsedAt;
    private Long usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
