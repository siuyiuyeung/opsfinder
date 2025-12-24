package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.excel.*;
import com.igsl.opsfinder.entity.ExcelCell;
import com.igsl.opsfinder.entity.ExcelFile;
import com.igsl.opsfinder.entity.ExcelSheet;
import com.igsl.opsfinder.exception.BadRequestException;
import com.igsl.opsfinder.exception.ResourceNotFoundException;
import com.igsl.opsfinder.exception.UnauthorizedException;
import com.igsl.opsfinder.mapper.ExcelFileMapper;
import com.igsl.opsfinder.repository.ExcelCellRepository;
import com.igsl.opsfinder.repository.ExcelFileRepository;
import com.igsl.opsfinder.repository.ExcelSheetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Main service for Excel file operations.
 * Orchestrates parsing, storage, indexing, and search functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelFileService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ExcelFileRepository excelFileRepository;
    private final ExcelSheetRepository excelSheetRepository;
    private final ExcelCellRepository excelCellRepository;
    private final ExcelParsingService excelParsingService;
    private final ExcelStorageService excelStorageService;
    private final ExcelIndexingService excelIndexingService;
    private final ExcelSearchService excelSearchService;
    private final ExcelSecurityService excelSecurityService;
    private final ExcelFileMapper excelFileMapper;

    @Value("${excel.storage.max-file-size:10485760}")
    private long maxFileSize;

    /**
     * Upload and index an Excel file.
     * Parses, stores, and indexes the file in a transactional manner.
     *
     * @param file the uploaded Excel file
     * @return response with file metadata
     * @throws BadRequestException if file is invalid or processing fails
     */
    @Transactional
    public ExcelFileResponse uploadExcelFile(MultipartFile file) {
        String currentUsername = getCurrentUsername();
        log.info("Processing Excel file upload: {} (user: {})",
                file.getOriginalFilename(), currentUsername);

        // Validate file
        validateFile(file);

        String filePath = null;
        try {
            // Step 1: Parse Excel file
            ExcelFileData fileData = excelParsingService.parseExcelFile(file);

            // Step 2: Store file on disk
            filePath = excelStorageService.storeFile(file, file.getOriginalFilename());

            // Step 3: Index data in database
            ExcelFile excelFile = excelIndexingService.indexExcelFile(
                    fileData, filePath, currentUsername);

            log.info("Successfully uploaded and indexed Excel file: {} (ID: {})",
                    file.getOriginalFilename(), excelFile.getId());

            return excelFileMapper.toResponse(excelFile);

        } catch (Exception e) {
            // Rollback: delete physical file if indexing fails
            if (filePath != null) {
                log.warn("Rolling back file storage due to error: {}", e.getMessage());
                excelStorageService.deleteFile(filePath);
            }
            throw e;
        }
    }

    /**
     * Get all Excel files with pagination and optional user filtering.
     *
     * @param pageable pagination parameters
     * @param uploadedBy optional username filter
     * @return page of Excel file responses
     */
    @Transactional(readOnly = true)
    public Page<ExcelFileResponse> getAllExcelFiles(Pageable pageable, String uploadedBy) {
        log.debug("Getting all Excel files - uploadedBy: {}", uploadedBy);

        Page<ExcelFile> files;

        if (uploadedBy != null && !uploadedBy.isEmpty()) {
            files = excelFileRepository.findByStatusAndUploadedBy(
                    ExcelFile.Status.ACTIVE, uploadedBy, pageable);
        } else {
            files = excelFileRepository.findByStatus(ExcelFile.Status.ACTIVE, pageable);
        }

        return files.map(excelFileMapper::toResponse);
    }

    /**
     * Get Excel file by ID with full details (sheets and headers).
     *
     * @param id the file ID
     * @return detailed file response with sheets
     * @throws ResourceNotFoundException if file not found
     */
    @Transactional(readOnly = true)
    public ExcelFileDetailResponse getExcelFileById(Long id) {
        log.debug("Getting Excel file by ID: {}", id);

        ExcelFile excelFile = excelFileRepository.findByIdWithSheets(id)
                .orElseThrow(() -> new ResourceNotFoundException("Excel file not found with ID: " + id));

        if (excelFile.getStatus() == ExcelFile.Status.DELETED) {
            throw new ResourceNotFoundException("Excel file has been deleted: " + id);
        }

        return excelFileMapper.toDetailResponse(excelFile);
    }

    /**
     * Delete an Excel file (soft delete + physical file removal).
     * Checks permissions: ADMIN can delete any, OPERATOR can delete own only.
     *
     * @param id the file ID
     * @param currentUsername the current user's username
     * @throws ResourceNotFoundException if file not found
     * @throws UnauthorizedException if user doesn't have permission
     */
    @Transactional
    public void deleteExcelFile(Long id, String currentUsername) {
        log.info("Deleting Excel file: {} (user: {})", id, currentUsername);

        ExcelFile excelFile = excelFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Excel file not found with ID: " + id));

        if (excelFile.getStatus() == ExcelFile.Status.DELETED) {
            throw new ResourceNotFoundException("Excel file already deleted: " + id);
        }

        // Check permissions
        Set<String> roles = getCurrentUserRoles();
        excelSecurityService.checkDeletePermission(id, currentUsername, roles);

        // Soft delete in database
        excelFile.setStatus(ExcelFile.Status.DELETED);
        excelFileRepository.save(excelFile);

        // Delete physical file
        excelStorageService.deleteFile(excelFile.getFilePath());

        log.info("Successfully deleted Excel file: {}", id);
    }

    /**
     * Search Excel data with multi-keyword AND logic.
     *
     * @param request search request with keywords and filters
     * @param pageable pagination parameters
     * @return page of search results with full row context
     */
    @Transactional(readOnly = true)
    public Page<ExcelSearchResultResponse> searchExcelData(ExcelSearchRequest request, Pageable pageable) {
        log.debug("Searching Excel data: {}", request);

        Page<ExcelCell> cells = excelSearchService.searchExcelData(request, pageable);

        return cells.map(cell -> {
            // Eagerly initialize the lazy-loaded relationships within transaction
            ExcelSheet sheet = cell.getExcelSheet();
            sheet.getSheetName(); // Force initialization
            sheet.getExcelFile().getOriginalFilename(); // Force initialization

            Long sheetId = sheet.getId();
            Integer rowNumber = cell.getRowNumber();
            Long cellId = cell.getId();

            log.debug("Fetching row data for sheetId: {}, rowNumber: {}", sheetId, rowNumber);

            ExcelSearchResultResponse response = excelFileMapper.toSearchResultResponse(cell);

            // Fetch all cells in the matched row for full context
            List<ExcelCell> rowCells = excelCellRepository.findBySheetIdAndRowNumber(sheetId, rowNumber);

            log.debug("Found {} cells in row {}", rowCells.size(), rowNumber);

            // Convert to RowCellData DTOs
            List<com.igsl.opsfinder.dto.excel.RowCellData> rowData = rowCells.stream()
                    .map(rowCell -> com.igsl.opsfinder.dto.excel.RowCellData.builder()
                            .columnHeader(rowCell.getColumnHeader())
                            .columnIndex(rowCell.getColumnIndex())
                            .cellValue(rowCell.getCellValue())
                            .isMatchedCell(rowCell.getId().equals(cellId))
                            .build())
                    .toList();

            response.setRowData(rowData);
            log.debug("Set {} row cells for response", rowData.size());
            return response;
        });
    }

    /**
     * Get statistics for all Excel files.
     *
     * @return statistics response
     */
    @Transactional(readOnly = true)
    public ExcelStatsResponse getStatistics() {
        log.debug("Getting Excel file statistics");

        long totalFiles = excelFileRepository.count();
        long activeFiles = excelFileRepository.countByStatus(ExcelFile.Status.ACTIVE);
        long totalSheets = excelSheetRepository.countAllActive();
        long totalCells = excelCellRepository.countAllActive();
        Long totalStorage = excelFileRepository.getTotalStorageUsed();

        return ExcelStatsResponse.builder()
                .totalFiles(totalFiles)
                .activeFiles(activeFiles)
                .totalSheets(totalSheets)
                .totalCells(totalCells)
                .totalStorageBytes(totalStorage != null ? totalStorage : 0L)
                .build();
    }

    /**
     * Validate uploaded file.
     *
     * @param file the file to validate
     * @throws BadRequestException if validation fails
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty or not provided");
        }

        // Check file size
        long fileSize = file.getSize();
        if (fileSize > maxFileSize) {
            throw new BadRequestException(
                    String.format("File size exceeds maximum allowed size of %d MB",
                            maxFileSize / 1024 / 1024));
        }

        // Check content type and extension
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        boolean validContentType = EXCEL_CONTENT_TYPE.equals(contentType);
        boolean validExtension = filename != null && filename.toLowerCase().endsWith(".xlsx");

        if (!validContentType && !validExtension) {
            throw new BadRequestException("File must be an Excel file (.xlsx)");
        }
    }

    /**
     * Get current authenticated username.
     *
     * @return username
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }

    /**
     * Get current user's roles.
     *
     * @return set of role names
     */
    private Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }
}
