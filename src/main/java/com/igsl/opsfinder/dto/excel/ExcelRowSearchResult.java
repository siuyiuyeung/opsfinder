package com.igsl.opsfinder.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for row-grouped Excel search results.
 * Groups multiple cell matches from the same row into a single result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelRowSearchResult {

    private Long fileId;
    private String fileName;
    private Long sheetId;
    private String sheetName;
    private Integer rowNumber;

    /**
     * List of matched cell values in this row.
     */
    private List<String> matchedValues;

    /**
     * Column indices of matched cells.
     */
    private List<Integer> matchedColumnIndices;

    /**
     * All cells in the row for full context.
     * Matched cells have isMatchedCell = true.
     */
    private List<RowCellData> rowData;
}
