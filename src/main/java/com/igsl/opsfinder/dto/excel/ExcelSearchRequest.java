package com.igsl.opsfinder.dto.excel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Excel cell search operations.
 * Supports multi-keyword AND logic at row level with optional file/sheet filtering.
 * For multiple keywords, all must appear somewhere in the same row (across different cells).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelSearchRequest {

    /**
     * Comma-separated keywords for search.
     * All keywords must appear in the same row (AND logic at row level).
     * Example: "apple,fruit,red" will find rows where all three words appear across different cells.
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
