package com.igsl.opsfinder.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for Excel search results.
 * Contains matched cell value with context information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelSearchResultResponse {

    private Long cellId;
    private String fileName;
    private String sheetName;
    private String columnHeader;
    private Integer rowNumber;
    private Integer columnIndex;
    private String cellValue;
    private Long fileId;
    private Long sheetId;

    /**
     * All cells in the matched row for full context.
     * Ordered by column index.
     */
    private List<RowCellData> rowData;
}
