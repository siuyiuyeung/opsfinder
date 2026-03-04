package com.igsl.opsfinder.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Response DTO returned only once on API key creation.
 * Contains the plaintext key which is never stored and cannot be retrieved again.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiKeyCreatedResponse extends ApiKeyResponse {

    private String plainTextKey;
}
