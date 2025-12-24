package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.ExcelFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ExcelFile entity.
 * Provides data access methods for Excel file management.
 */
@Repository
public interface ExcelFileRepository extends JpaRepository<ExcelFile, Long> {

    /**
     * Find Excel files by status.
     *
     * @param status the file status
     * @param pageable pagination parameters
     * @return page of Excel files
     */
    Page<ExcelFile> findByStatus(ExcelFile.Status status, Pageable pageable);

    /**
     * Find Excel files by uploader username.
     *
     * @param uploadedBy the username who uploaded the file
     * @param pageable pagination parameters
     * @return page of Excel files
     */
    Page<ExcelFile> findByUploadedBy(String uploadedBy, Pageable pageable);

    /**
     * Find Excel files by status and uploader username.
     *
     * @param status the file status
     * @param uploadedBy the username who uploaded the file
     * @param pageable pagination parameters
     * @return page of Excel files
     */
    Page<ExcelFile> findByStatusAndUploadedBy(ExcelFile.Status status, String uploadedBy, Pageable pageable);

    /**
     * Find Excel file by ID with sheets eagerly loaded.
     *
     * @param id the file ID
     * @return optional Excel file with sheets
     */
    @Query("SELECT ef FROM ExcelFile ef LEFT JOIN FETCH ef.sheets WHERE ef.id = :id")
    Optional<ExcelFile> findByIdWithSheets(Long id);

    /**
     * Count files by status.
     *
     * @param status the file status
     * @return number of files with the specified status
     */
    long countByStatus(ExcelFile.Status status);

    /**
     * Count files by uploader username.
     *
     * @param uploadedBy the username who uploaded the file
     * @return number of files uploaded by the user
     */
    long countByUploadedBy(String uploadedBy);

    /**
     * Get total storage used by all active files.
     *
     * @return sum of file sizes in bytes
     */
    @Query("SELECT COALESCE(SUM(ef.fileSize), 0) FROM ExcelFile ef WHERE ef.status = 'ACTIVE'")
    Long getTotalStorageUsed();

    /**
     * Check if a file exists by ID and status.
     *
     * @param id the file ID
     * @param status the file status
     * @return true if file exists with given status
     */
    boolean existsByIdAndStatus(Long id, ExcelFile.Status status);
}
