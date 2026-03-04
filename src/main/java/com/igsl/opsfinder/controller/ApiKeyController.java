package com.igsl.opsfinder.controller;

import com.igsl.opsfinder.dto.request.ApiKeyCreateRequest;
import com.igsl.opsfinder.dto.response.ApiKeyCreatedResponse;
import com.igsl.opsfinder.dto.response.ApiKeyResponse;
import com.igsl.opsfinder.dto.response.ApiKeyStatsResponse;
import com.igsl.opsfinder.dto.response.ApiKeyUsageLogResponse;
import com.igsl.opsfinder.service.ApiKeyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller for API key management.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/api-keys")
@PreAuthorize("hasRole('ADMIN')")
public class ApiKeyController {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyController.class);

    @Autowired
    private ApiKeyService apiKeyService;

    /**
     * Create a new API key.
     */
    @PostMapping
    public ResponseEntity<ApiKeyCreatedResponse> createApiKey(
            @Valid @RequestBody ApiKeyCreateRequest request) {
        logger.info("Admin request: Create API key '{}'", request.getName());
        ApiKeyCreatedResponse response = apiKeyService.createApiKey(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List all API keys (paginated).
     */
    @GetMapping
    public ResponseEntity<Page<ApiKeyResponse>> listApiKeys(Pageable pageable) {
        logger.info("Admin request: List API keys");
        return ResponseEntity.ok(apiKeyService.listApiKeys(pageable));
    }

    /**
     * Get a single API key by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiKeyResponse> getApiKey(@PathVariable Long id) {
        logger.info("Admin request: Get API key id={}", id);
        return ResponseEntity.ok(apiKeyService.getApiKey(id));
    }

    /**
     * Revoke an API key (sets active=false).
     */
    @PatchMapping("/{id}/revoke")
    public ResponseEntity<ApiKeyResponse> revokeApiKey(@PathVariable Long id) {
        logger.info("Admin request: Revoke API key id={}", id);
        return ResponseEntity.ok(apiKeyService.revokeApiKey(id));
    }

    /**
     * Delete an API key and all its usage logs.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteApiKey(@PathVariable Long id) {
        logger.info("Admin request: Delete API key id={}", id);
        apiKeyService.deleteApiKey(id);
        return ResponseEntity.ok(Map.of("message", "API key deleted successfully"));
    }

    /**
     * Get usage logs for an API key with optional date-range filter.
     */
    @GetMapping("/{id}/usage")
    public ResponseEntity<Page<ApiKeyUsageLogResponse>> getUsageLogs(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        logger.info("Admin request: Get usage logs for API key id={}", id);
        return ResponseEntity.ok(apiKeyService.getUsageLogs(id, from, to, pageable));
    }

    /**
     * Get aggregate statistics across all API keys.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiKeyStatsResponse> getStats() {
        logger.info("Admin request: Get API key stats");
        return ResponseEntity.ok(apiKeyService.getStats());
    }
}
