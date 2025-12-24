package com.igsl.opsfinder.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Excel file statistics.
 * Provides aggregate information across all Excel files.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelStatsResponse {

    private Long totalFiles;
    private Long activeFiles;
    private Long totalSheets;
    private Long totalCells;
    private Long totalStorageBytes;
}
