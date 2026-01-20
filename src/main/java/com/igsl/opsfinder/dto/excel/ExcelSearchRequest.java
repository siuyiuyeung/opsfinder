package com.igsl.opsfinder.dto.excel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * Optional: Filter results to specific Excel file IDs.
     * When multiple files are selected, results from all selected files are returned.
     */
    private List<Long> fileIds;

    /**
     * Optional: Filter results to specific sheet names (case-insensitive).
     * When multiple sheets are selected, results from all selected sheets are returned.
     */
    private List<String> sheetNames;
}
