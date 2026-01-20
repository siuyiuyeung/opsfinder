package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.ExcelCell;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Custom repository interface for Excel cell search operations.
 * Supports dynamic multi-keyword search with variable keyword count.
 */
public interface ExcelCellRepositoryCustom {

    /**
     * Search Excel cells with dynamic multi-keyword AND logic at row level.
     * For single keyword: returns all cells containing that keyword.
     * For multiple keywords: returns cells from rows where ALL keywords appear somewhere in the row.
     * All matching is case-insensitive and supports partial matches.
     *
     * @param fileIds optional list of file IDs to filter (empty list or null means no filter)
     * @param sheetNames optional list of sheet names to filter (case-insensitive, empty list or null means no filter)
     * @param keywords list of keywords (all required)
     * @param pageable pagination parameters
     * @return page of matching cells from rows where all keywords appear
     */
    Page<ExcelCell> searchWithDynamicKeywords(
            List<Long> fileIds,
            List<String> sheetNames,
            List<String> keywords,
            Pageable pageable
    );
}
