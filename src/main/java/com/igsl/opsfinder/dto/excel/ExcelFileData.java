package com.igsl.opsfinder.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal DTO for parsed Excel file data.
 * Used to transfer parsed data from ExcelParsingService to ExcelIndexingService.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelFileData {

    private String originalFilename;
    private Long fileSize;

    @Builder.Default
    private List<SheetData> sheets = new ArrayList<>();

    /**
     * Get total number of rows across all sheets.
     *
     * @return total row count
     */
    public long getTotalRowCount() {
        return sheets.stream()
                .mapToLong(sheet -> sheet.getRows().size())
                .sum();
    }

    /**
     * Get total number of cells across all sheets.
     *
     * @return total cell count
     */
    public long getTotalCellCount() {
        return sheets.stream()
                .mapToLong(sheet -> sheet.getRows().stream()
                        .mapToLong(row -> row.getCellValues().size())
                        .sum())
                .sum();
    }

    /**
     * Represents a single sheet within the Excel file.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SheetData {

        private String sheetName;
        private Integer sheetIndex;

        @Builder.Default
        private List<String> headers = new ArrayList<>();

        @Builder.Default
        private List<RowData> rows = new ArrayList<>();

        /**
         * Get column count from header size.
         *
         * @return number of columns
         */
        public int getColumnCount() {
            return headers.size();
        }
    }

    /**
     * Represents a single row within a sheet.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowData {

        private Integer rowNumber;

        @Builder.Default
        private List<String> cellValues = new ArrayList<>();
    }
}
