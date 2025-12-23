package com.igsl.opsfinder.dto.csv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for CSV import operation results.
 * Provides detailed row-by-row success/failure reporting.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceImportResult {

    private int totalRows;
    private int successCount;
    private int failureCount;

    @Builder.Default
    private List<RowResult> results = new ArrayList<>();

    /**
     * Add a successful import result.
     *
     * @param rowNumber 1-indexed row number (header = row 0)
     * @param deviceData the device data that was imported
     */
    public void addSuccess(int rowNumber, DeviceImportDto deviceData) {
        results.add(RowResult.builder()
                .rowNumber(rowNumber)
                .success(true)
                .deviceData(deviceData)
                .build());
        successCount++;
    }

    /**
     * Add a failed import result.
     *
     * @param rowNumber 1-indexed row number (header = row 0)
     * @param errorMessage the error message describing the failure
     * @param deviceData the device data that failed to import
     */
    public void addFailure(int rowNumber, String errorMessage, DeviceImportDto deviceData) {
        results.add(RowResult.builder()
                .rowNumber(rowNumber)
                .success(false)
                .errorMessage(errorMessage)
                .deviceData(deviceData)
                .build());
        failureCount++;
    }

    /**
     * Result for a single CSV row.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RowResult {
        private int rowNumber;  // 1-indexed (header = row 0)
        private boolean success;
        private String errorMessage;
        private DeviceImportDto deviceData;  // Optional: include data for context
    }
}
