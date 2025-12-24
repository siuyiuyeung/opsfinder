package com.igsl.opsfinder.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Excel file basic information.
 * Used in list operations and after upload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelFileResponse {

    private Long id;
    private String originalFilename;
    private Long fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private Integer sheetCount;
    private Long rowCount;
    private Long cellCount;
    private String status;
}
