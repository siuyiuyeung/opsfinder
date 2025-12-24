package com.igsl.opsfinder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an uploaded Excel file.
 * Stores metadata about the file and maintains relationships with its sheets.
 */
@Entity
@Table(name = "excel_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcelFile extends BaseEntity {

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 255)
    private String storedFilename;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "uploaded_by", nullable = false, length = 100)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "sheet_count", nullable = false)
    private Integer sheetCount;

    @Column(name = "row_count", nullable = false)
    private Long rowCount;

    @Column(name = "cell_count", nullable = false)
    private Long cellCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @OneToMany(mappedBy = "excelFile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExcelSheet> sheets = new ArrayList<>();

    /**
     * Status of the Excel file.
     */
    public enum Status {
        /** File is active and available for search */
        ACTIVE,
        /** File has been soft-deleted */
        DELETED
    }

    /**
     * Helper method to add a sheet to this file.
     *
     * @param sheet the sheet to add
     */
    public void addSheet(ExcelSheet sheet) {
        sheets.add(sheet);
        sheet.setExcelFile(this);
    }

    /**
     * Helper method to remove a sheet from this file.
     *
     * @param sheet the sheet to remove
     */
    public void removeSheet(ExcelSheet sheet) {
        sheets.remove(sheet);
        sheet.setExcelFile(null);
    }

    @PrePersist
    protected void onCreateFile() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "ExcelFile{" +
                "id=" + getId() +
                ", originalFilename='" + originalFilename + '\'' +
                ", storedFilename='" + storedFilename + '\'' +
                ", fileSize=" + fileSize +
                ", uploadedBy='" + uploadedBy + '\'' +
                ", uploadedAt=" + uploadedAt +
                ", sheetCount=" + sheetCount +
                ", status=" + status +
                '}';
    }
}
