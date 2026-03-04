package com.igsl.opsfinder.dto.response;

import lombok.Data;

/**
 * Aggregate statistics response for API keys.
 */
@Data
public class ApiKeyStatsResponse {

    private long totalKeys;
    private long activeKeys;
    private long totalRequests;
    private long requestsLast24h;
}
