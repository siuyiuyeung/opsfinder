package com.igsl.opsfinder.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a single cell within an Excel sheet.
 * Stores cell data optimized for search operations.
 */
@Entity
@Table(name = "excel_cells", indexes = {
        @Index(name = "idx_excel_cells_sheet_id", columnList = "excel_sheet_id"),
        @Index(name = "idx_excel_cells_value_lower", columnList = "cell_value_lower"),
        @Index(name = "idx_excel_cells_column_header", columnList = "column_header")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelCell extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "excel_sheet_id", nullable = false)
    private ExcelSheet excelSheet;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "column_index", nullable = false)
    private Integer columnIndex;

    @Column(name = "column_header", length = 255)
    private String columnHeader;

    @Column(name = "cell_value", columnDefinition = "TEXT")
    private String cellValue;

    @Column(name = "cell_value_lower", columnDefinition = "TEXT")
    private String cellValueLower;

    /**
     * Automatically set cell_value_lower before persisting or updating.
     */
    @PrePersist
    @PreUpdate
    protected void onSaveOrUpdate() {
        if (cellValue != null) {
            this.cellValueLower = cellValue.toLowerCase();
        } else {
            this.cellValueLower = null;
        }
    }

    @Override
    public String toString() {
        return "ExcelCell{" +
                "id=" + getId() +
                ", rowNumber=" + rowNumber +
                ", columnIndex=" + columnIndex +
                ", columnHeader='" + columnHeader + '\'' +
                ", cellValue='" + (cellValue != null && cellValue.length() > 50
                        ? cellValue.substring(0, 50) + "..."
                        : cellValue) + '\'' +
                ", excelSheetId=" + (excelSheet != null ? excelSheet.getId() : null) +
                '}';
    }
}
