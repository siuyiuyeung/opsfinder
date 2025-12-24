package com.igsl.opsfinder.dto.excel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Excel cell search operations.
 * Supports multi-keyword AND logic with optional file/sheet filtering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelSearchRequest {

    /**
     * Comma-separated keywords for search.
     * All keywords must match (AND logic).
     * Example: "apple,fruit,red" will find cells containing all three words.
     */
    @NotBlank(message = "Keywords are required for search")
    private String keywords;

    /**
     * Optional: Filter results to a specific Excel file ID.
     */
    private Long fileId;

    /**
     * Optional: Filter results to a specific sheet name (case-insensitive).
     */
    private String sheetName;
}
