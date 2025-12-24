package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.ExcelCell;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ExcelCell entity.
 * Provides data access methods for Excel cell search operations.
 */
@Repository
public interface ExcelCellRepository extends JpaRepository<ExcelCell, Long> {

    /**
     * Count cells in a specific Excel sheet.
     *
     * @param excelSheetId the Excel sheet ID
     * @return number of cells in the sheet
     */
    long countByExcelSheetId(Long excelSheetId);

    /**
     * Get total number of cells across all active files.
     *
     * @return total cell count
     */
    @Query("SELECT COUNT(ec) FROM ExcelCell ec " +
           "JOIN ec.excelSheet es " +
           "JOIN es.excelFile ef " +
           "WHERE ef.status = 'ACTIVE'")
    long countAllActive();

    /**
     * Search Excel cells with multi-keyword AND logic.
     * All keywords must match (case-insensitive partial matching).
     * Supports filtering by file ID and/or sheet name.
     * Uses native SQL with explicit type casting to avoid PostgreSQL type inference issues.
     *
     * @param fileId optional file ID filter
     * @param sheetName optional sheet name filter (case-insensitive)
     * @param keyword1 first keyword (required)
     * @param keyword2 second keyword (optional)
     * @param keyword3 third keyword (optional)
     * @param keyword4 fourth keyword (optional)
     * @param keyword5 fifth keyword (optional)
     * @param pageable pagination parameters
     * @return page of matching cells
     */
    @Query(value = "SELECT ec.* FROM excel_cells ec " +
           "JOIN excel_sheets es ON ec.excel_sheet_id = es.id " +
           "JOIN excel_files ef ON es.excel_file_id = ef.id " +
           "WHERE ef.status = 'ACTIVE' " +
           "AND (:fileId IS NULL OR ef.id = :fileId) " +
           "AND (:sheetName IS NULL OR LOWER(es.sheet_name) = LOWER(CAST(:sheetName AS TEXT))) " +
           "AND (:keyword1 IS NULL OR LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword1 AS TEXT), '%'))) " +
           "AND (:keyword2 IS NULL OR LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword2 AS TEXT), '%'))) " +
           "AND (:keyword3 IS NULL OR LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword3 AS TEXT), '%'))) " +
           "AND (:keyword4 IS NULL OR LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword4 AS TEXT), '%'))) " +
           "AND (:keyword5 IS NULL OR LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword5 AS TEXT), '%')))",
           nativeQuery = true)
    Page<ExcelCell> searchWithMultipleKeywords(
            Long fileId,
            String sheetName,
            String keyword1,
            String keyword2,
            String keyword3,
            String keyword4,
            String keyword5,
            Pageable pageable
    );

    /**
     * Search Excel cells with a single keyword.
     * Simpler variant for single keyword searches.
     * Uses native SQL with explicit type casting to avoid PostgreSQL type inference issues.
     *
     * @param fileId optional file ID filter
     * @param sheetName optional sheet name filter (case-insensitive)
     * @param keyword search keyword (case-insensitive partial match)
     * @param pageable pagination parameters
     * @return page of matching cells
     */
    @Query(value = "SELECT ec.* FROM excel_cells ec " +
           "JOIN excel_sheets es ON ec.excel_sheet_id = es.id " +
           "JOIN excel_files ef ON es.excel_file_id = ef.id " +
           "WHERE ef.status = 'ACTIVE' " +
           "AND (:fileId IS NULL OR ef.id = :fileId) " +
           "AND (:sheetName IS NULL OR LOWER(es.sheet_name) = LOWER(CAST(:sheetName AS TEXT))) " +
           "AND LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword AS TEXT), '%'))",
           nativeQuery = true)
    Page<ExcelCell> searchWithSingleKeyword(
            Long fileId,
            String sheetName,
            String keyword,
            Pageable pageable
    );

    /**
     * Find cells by sheet ID with pagination.
     *
     * @param excelSheetId the Excel sheet ID
     * @param pageable pagination parameters
     * @return page of cells
     */
    @Query("SELECT ec FROM ExcelCell ec WHERE ec.excelSheet.id = :excelSheetId " +
           "ORDER BY ec.rowNumber, ec.columnIndex")
    Page<ExcelCell> findByExcelSheetId(Long excelSheetId, Pageable pageable);

    /**
     * Find cells by file ID with pagination.
     *
     * @param excelFileId the Excel file ID
     * @param pageable pagination parameters
     * @return page of cells
     */
    @Query("SELECT ec FROM ExcelCell ec " +
           "JOIN ec.excelSheet es " +
           "WHERE es.excelFile.id = :excelFileId " +
           "ORDER BY es.sheetIndex, ec.rowNumber, ec.columnIndex")
    Page<ExcelCell> findByExcelFileId(Long excelFileId, Pageable pageable);

    /**
     * Find all cells in a specific row of a sheet.
     * Ordered by column index for proper display order.
     *
     * @param excelSheetId the Excel sheet ID
     * @param rowNumber the row number
     * @return list of cells in the row
     */
    @Query("SELECT ec FROM ExcelCell ec " +
           "WHERE ec.excelSheet.id = :excelSheetId " +
           "AND ec.rowNumber = :rowNumber " +
           "ORDER BY ec.columnIndex")
    List<ExcelCell> findBySheetIdAndRowNumber(Long excelSheetId, Integer rowNumber);
}
