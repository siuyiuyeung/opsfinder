package com.igsl.opsfinder.controller;

import com.igsl.opsfinder.dto.excel.*;
import com.igsl.opsfinder.service.ExcelFileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

/**
 * REST controller for Excel file management and search operations.
 * Endpoints are protected with role-based access control.
 */
@RestController
@RequestMapping("/api/excel-files")
@RequiredArgsConstructor
@Slf4j
public class ExcelFileController {

    private final ExcelFileService excelFileService;

    /**
     * Upload an Excel file.
     * Parses, stores, and indexes the file for search.
     * Accessible by ADMIN and OPERATOR roles only.
     *
     * @param file the uploaded Excel file
     * @return uploaded file response with metadata
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ExcelFileResponse> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        log.info("Upload Excel file request - filename: {}, size: {} bytes",
                file.getOriginalFilename(), file.getSize());

        ExcelFileResponse response = excelFileService.uploadExcelFile(file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all Excel files with pagination and optional filtering.
     * Accessible by all authenticated users.
     *
     * @param uploadedBy optional username filter
     * @param page page number (0-indexed)
     * @param size page size
     * @param sort sort field and direction
     * @return page of Excel files
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ExcelFileResponse>> getAllExcelFiles(
            @RequestParam(required = false) String uploadedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt,desc") String sort) {

        log.info("Get all Excel files request - uploadedBy: {}, page: {}, size: {}",
                uploadedBy, page, size);

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<ExcelFileResponse> files = excelFileService.getAllExcelFiles(pageable, uploadedBy);

        return ResponseEntity.ok(files);
    }

    /**
     * Get Excel file by ID with full details (sheets and headers).
     * Accessible by all authenticated users.
     *
     * @param id the file ID
     * @return detailed file response
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExcelFileDetailResponse> getExcelFileById(@PathVariable Long id) {
        log.info("Get Excel file by ID request - id: {}", id);

        ExcelFileDetailResponse response = excelFileService.getExcelFileById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete an Excel file by ID.
     * ADMIN can delete any file, OPERATOR can delete own files only.
     *
     * @param id the file ID
     * @param principal the authenticated user
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('OPERATOR') and @excelSecurityService.isOwner(#id, principal.username))")
    public ResponseEntity<Void> deleteExcelFile(@PathVariable Long id, Principal principal) {
        log.info("Delete Excel file request - id: {} (user: {})", id, principal.getName());

        excelFileService.deleteExcelFile(id, principal.getName());

        return ResponseEntity.noContent().build();
    }

    /**
     * Search Excel data with multi-keyword AND logic at row level.
     * For multiple keywords, all must appear somewhere in the same row (across different cells).
     * Returns one result per row with all matched cells highlighted.
     * Supports filtering by multiple file IDs and/or sheet names.
     * Accessible by all authenticated users.
     *
     * @param keywords comma-separated keywords (required)
     * @param fileIds optional file ID filter (comma-separated for multiple)
     * @param sheetNames optional sheet name filter (comma-separated for multiple)
     * @param page page number (0-indexed)
     * @param size page size
     * @return page of row-grouped search results
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ExcelRowSearchResult>> searchExcelData(
            @RequestParam String keywords,
            @RequestParam(required = false) List<Long> fileIds,
            @RequestParam(required = false) List<String> sheetNames,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Search Excel data request - keywords: {}, fileIds: {}, sheetNames: {}, page: {}, size: {}",
                keywords, fileIds, sheetNames, page, size);

        ExcelSearchRequest request = ExcelSearchRequest.builder()
                .keywords(keywords)
                .fileIds(fileIds)
                .sheetNames(sheetNames)
                .build();

        Pageable pageable = PageRequest.of(page, size);

        Page<ExcelRowSearchResult> results = excelFileService.searchExcelData(request, pageable);

        return ResponseEntity.ok(results);
    }

    /**
     * Get Excel file statistics.
     * Accessible by all authenticated users.
     *
     * @return statistics response
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExcelStatsResponse> getStatistics() {
        log.info("Get Excel file statistics request");

        ExcelStatsResponse stats = excelFileService.getStatistics();

        return ResponseEntity.ok(stats);
    }
}
