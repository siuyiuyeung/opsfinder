package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.ExcelSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ExcelSheet entity.
 * Provides data access methods for Excel sheet management.
 */
@Repository
public interface ExcelSheetRepository extends JpaRepository<ExcelSheet, Long> {

    /**
     * Find all sheets belonging to a specific Excel file.
     *
     * @param excelFileId the Excel file ID
     * @return list of sheets ordered by sheet index
     */
    @Query("SELECT es FROM ExcelSheet es WHERE es.excelFile.id = :excelFileId ORDER BY es.sheetIndex")
    List<ExcelSheet> findByExcelFileId(Long excelFileId);

    /**
     * Find a sheet by file ID and sheet name.
     * Uses native SQL with explicit type casting to avoid PostgreSQL type inference issues.
     *
     * @param excelFileId the Excel file ID
     * @param sheetName the sheet name (case-insensitive)
     * @return optional Excel sheet
     */
    @Query(value = "SELECT es.* FROM excel_sheets es " +
           "JOIN excel_files ef ON es.excel_file_id = ef.id " +
           "WHERE ef.id = :excelFileId " +
           "AND LOWER(es.sheet_name) = LOWER(CAST(:sheetName AS TEXT))",
           nativeQuery = true)
    Optional<ExcelSheet> findByExcelFileIdAndSheetName(Long excelFileId, String sheetName);

    /**
     * Find a sheet by file ID and sheet index.
     *
     * @param excelFileId the Excel file ID
     * @param sheetIndex the sheet index (0-based)
     * @return optional Excel sheet
     */
    Optional<ExcelSheet> findByExcelFileIdAndSheetIndex(Long excelFileId, Integer sheetIndex);

    /**
     * Count sheets in a specific Excel file.
     *
     * @param excelFileId the Excel file ID
     * @return number of sheets in the file
     */
    long countByExcelFileId(Long excelFileId);

    /**
     * Get total number of sheets across all active files.
     *
     * @return total sheet count
     */
    @Query("SELECT COUNT(es) FROM ExcelSheet es WHERE es.excelFile.status = 'ACTIVE'")
    long countAllActive();

    /**
     * Check if a sheet exists by file ID and sheet name.
     * Uses native SQL with explicit type casting to avoid PostgreSQL type inference issues.
     *
     * @param excelFileId the Excel file ID
     * @param sheetName the sheet name (case-insensitive)
     * @return true if sheet exists
     */
    @Query(value = "SELECT CASE WHEN COUNT(es.*) > 0 THEN true ELSE false END FROM excel_sheets es " +
           "WHERE es.excel_file_id = :excelFileId AND LOWER(es.sheet_name) = LOWER(CAST(:sheetName AS TEXT))",
           nativeQuery = true)
    boolean existsByExcelFileIdAndSheetName(Long excelFileId, String sheetName);
}
