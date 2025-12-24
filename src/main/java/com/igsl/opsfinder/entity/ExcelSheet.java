package com.igsl.opsfinder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a sheet within an Excel file.
 * Stores sheet metadata and maintains relationships with its cells.
 */
@Entity
@Table(name = "excel_sheets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelSheet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "excel_file_id", nullable = false)
    private ExcelFile excelFile;

    @Column(name = "sheet_name", nullable = false, length = 255)
    private String sheetName;

    @Column(name = "sheet_index", nullable = false)
    private Integer sheetIndex;

    @Column(name = "row_count", nullable = false)
    private Integer rowCount;

    @Column(name = "column_count", nullable = false)
    private Integer columnCount;

    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers;

    @OneToMany(mappedBy = "excelSheet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExcelCell> cells = new ArrayList<>();

    /**
     * Helper method to add a cell to this sheet.
     *
     * @param cell the cell to add
     */
    public void addCell(ExcelCell cell) {
        cells.add(cell);
        cell.setExcelSheet(this);
    }

    /**
     * Helper method to remove a cell from this sheet.
     *
     * @param cell the cell to remove
     */
    public void removeCell(ExcelCell cell) {
        cells.remove(cell);
        cell.setExcelSheet(null);
    }

    /**
     * Get headers as a list.
     * Headers are stored as comma-separated values in the database.
     *
     * @return list of header names, or empty list if headers is null
     */
    public List<String> getHeadersList() {
        if (headers == null || headers.isEmpty()) {
            return List.of();
        }
        return List.of(headers.split(","));
    }

    /**
     * Set headers from a list.
     * Converts list to comma-separated string for database storage.
     *
     * @param headersList list of header names
     */
    public void setHeadersList(List<String> headersList) {
        if (headersList == null || headersList.isEmpty()) {
            this.headers = null;
        } else {
            this.headers = String.join(",", headersList);
        }
    }

    @Override
    public String toString() {
        return "ExcelSheet{" +
                "id=" + getId() +
                ", sheetName='" + sheetName + '\'' +
                ", sheetIndex=" + sheetIndex +
                ", rowCount=" + rowCount +
                ", columnCount=" + columnCount +
                ", excelFileId=" + (excelFile != null ? excelFile.getId() : null) +
                '}';
    }
}
