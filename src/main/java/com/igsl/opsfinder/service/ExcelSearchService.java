package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.excel.ExcelSearchRequest;
import com.igsl.opsfinder.entity.ExcelCell;
import com.igsl.opsfinder.exception.BadRequestException;
import com.igsl.opsfinder.repository.ExcelCellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for searching Excel cell data.
 * Supports multi-keyword AND logic with file/sheet filtering.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelSearchService {

    private static final int MAX_KEYWORDS = 5;
    private static final int MAX_KEYWORD_LENGTH = 200;

    private final ExcelCellRepository excelCellRepository;

    /**
     * Search Excel data with multi-keyword AND logic.
     * All keywords must match (case-insensitive partial matching).
     * Results can be filtered by file ID and/or sheet name.
     *
     * @param request the search request with keywords and optional filters
     * @param pageable pagination parameters
     * @return page of matching Excel cells
     * @throws BadRequestException if keywords are invalid
     */
    @Transactional(readOnly = true)
    public Page<ExcelCell> searchExcelData(ExcelSearchRequest request, Pageable pageable) {
        log.info("Searching Excel data - keywords: {}, fileId: {}, sheetName: {}",
                request.getKeywords(), request.getFileId(), request.getSheetName());

        // Parse and validate keywords
        List<String> keywords = parseKeywords(request.getKeywords());

        // Validate keyword count
        if (keywords.isEmpty()) {
            throw new BadRequestException("At least one keyword is required for search");
        }

        if (keywords.size() > MAX_KEYWORDS) {
            throw new BadRequestException(
                    String.format("Too many keywords: %d (max: %d)", keywords.size(), MAX_KEYWORDS));
        }

        // Execute search based on keyword count
        Page<ExcelCell> results;

        if (keywords.size() == 1) {
            // Use optimized single-keyword search
            results = excelCellRepository.searchWithSingleKeyword(
                    request.getFileId(),
                    request.getSheetName(),
                    keywords.get(0),
                    pageable
            );
        } else {
            // Use multi-keyword AND search
            results = excelCellRepository.searchWithMultipleKeywords(
                    request.getFileId(),
                    request.getSheetName(),
                    keywords.size() > 0 ? keywords.get(0) : null,
                    keywords.size() > 1 ? keywords.get(1) : null,
                    keywords.size() > 2 ? keywords.get(2) : null,
                    keywords.size() > 3 ? keywords.get(3) : null,
                    keywords.size() > 4 ? keywords.get(4) : null,
                    pageable
            );
        }

        log.info("Search completed - found {} results out of {} total",
                results.getNumberOfElements(), results.getTotalElements());

        return results;
    }

    /**
     * Parse comma-separated keywords into a list.
     * Trims whitespace and filters out empty keywords.
     *
     * @param keywordsString comma-separated keywords
     * @return list of parsed keywords
     * @throws BadRequestException if keywords string is too long
     */
    private List<String> parseKeywords(String keywordsString) {
        if (keywordsString == null || keywordsString.trim().isEmpty()) {
            return List.of();
        }

        // Validate total length
        if (keywordsString.length() > MAX_KEYWORD_LENGTH) {
            throw new BadRequestException(
                    String.format("Keywords string too long: %d characters (max: %d)",
                            keywordsString.length(), MAX_KEYWORD_LENGTH));
        }

        // Split by comma and clean up
        String[] parts = keywordsString.split(",");
        List<String> keywords = new ArrayList<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                keywords.add(trimmed);
            }
        }

        return keywords;
    }

    /**
     * Count total searchable cells across all active files.
     *
     * @return total cell count
     */
    @Transactional(readOnly = true)
    public long countAllSearchableCells() {
        return excelCellRepository.countAllActive();
    }
}
