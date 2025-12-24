package com.igsl.opsfinder.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Detailed response DTO for Excel file with sheets and headers.
 * Used when retrieving a specific file by ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelFileDetailResponse {

    private Long id;
    private String originalFilename;
    private Long fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private Integer sheetCount;
    private Long rowCount;
    private Long cellCount;
    private String status;

    @Builder.Default
    private List<SheetInfo> sheets = new ArrayList<>();

    /**
     * Information about a single sheet within the Excel file.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SheetInfo {
        private Long sheetId;
        private String sheetName;
        private Integer sheetIndex;
        private Integer rowCount;
        private Integer columnCount;

        @Builder.Default
        private List<String> headers = new ArrayList<>();
    }
}
