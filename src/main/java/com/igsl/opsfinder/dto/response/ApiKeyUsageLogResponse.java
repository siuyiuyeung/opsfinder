package com.igsl.opsfinder.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for a single API key usage log entry.
 */
@Data
public class ApiKeyUsageLogResponse {

    private Long id;
    private Long apiKeyId;
    private String endpoint;
    private String httpMethod;
    private String clientIp;
    private Integer responseStatus;
    private Long responseTimeMs;
    private LocalDateTime requestedAt;
}
