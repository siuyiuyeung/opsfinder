package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.ExcelCell;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.NativeQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Custom repository implementation for dynamic Excel cell search operations.
 * Builds SQL queries dynamically based on the number of keywords provided.
 */
@Slf4j
public class ExcelCellRepositoryCustomImpl implements ExcelCellRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<ExcelCell> searchWithDynamicKeywords(
            List<Long> fileIds,
            List<String> sheetNames,
            List<String> keywords,
            Pageable pageable) {

        if (keywords == null || keywords.isEmpty()) {
            return Page.empty(pageable);
        }

        // Normalize empty lists to null for cleaner conditional logic
        List<Long> normalizedFileIds = (fileIds != null && !fileIds.isEmpty()) ? fileIds : null;
        List<String> normalizedSheetNames = (sheetNames != null && !sheetNames.isEmpty()) ? sheetNames : null;

        // Build the SQL query dynamically (conditionally includes filters based on NULL checks)
        String sql = buildSearchQuery(keywords, normalizedFileIds, normalizedSheetNames);
        String countSql = buildCountQuery(keywords, normalizedFileIds, normalizedSheetNames);

        // Create and configure the query using Hibernate NativeQuery for better type handling
        NativeQuery<ExcelCell> query = (NativeQuery<ExcelCell>) entityManager.createNativeQuery(sql, ExcelCell.class);
        NativeQuery<Long> countQuery = (NativeQuery<Long>) entityManager.createNativeQuery(countSql);

        // Set parameters - only set non-null parameters since SQL is built conditionally
        setQueryParameters(query, normalizedFileIds, normalizedSheetNames, keywords);
        setQueryParameters(countQuery, normalizedFileIds, normalizedSheetNames, keywords);

        // Execute count query
        long total = countQuery.getSingleResult();

        // Execute main query with pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<ExcelCell> results = query.getResultList();

        return new PageImpl<>(results, pageable, total);
    }

    /**
     * Build the main search query with dynamic keyword count.
     * Handles NULL parameters by conditional SQL building instead of SQL-level NULL checks.
     */
    private String buildSearchQuery(List<String> keywords, List<Long> fileIds, List<String> sheetNames) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ec.* FROM excel_cells ec ");
        sql.append("JOIN excel_sheets es ON ec.excel_sheet_id = es.id ");
        sql.append("JOIN excel_files ef ON es.excel_file_id = ef.id ");
        sql.append("WHERE ef.status = 'ACTIVE' ");

        // Only add fileIds filter if provided (use IN clause for multiple values)
        if (fileIds != null) {
            sql.append("AND ef.id IN (:fileIds) ");
        }

        // Only add sheetNames filter if provided (use IN clause for multiple values, case-insensitive)
        if (sheetNames != null) {
            sql.append("AND LOWER(es.sheet_name) IN (:sheetNamesLower) ");
        }

        // Cell must contain at least one keyword
        if (keywords.size() == 1) {
            // Optimized for single keyword
            sql.append("AND LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword0 AS TEXT), '%')) ");
        } else {
            // Multiple keywords - cell must match at least one
            sql.append("AND (");
            for (int i = 0; i < keywords.size(); i++) {
                if (i > 0) {
                    sql.append(" OR ");
                }
                sql.append("LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword").append(i).append(" AS TEXT), '%'))");
            }
            sql.append(") ");

            // Cell must be in a row where ALL keywords appear
            sql.append("AND (ec.excel_sheet_id, ec.row_number) IN (");
            sql.append("    SELECT sub.excel_sheet_id, sub.row_number ");
            sql.append("    FROM excel_cells sub ");
            sql.append("    JOIN excel_sheets sub_es ON sub.excel_sheet_id = sub_es.id ");
            sql.append("    JOIN excel_files sub_ef ON sub_es.excel_file_id = sub_ef.id ");
            sql.append("    WHERE sub_ef.status = 'ACTIVE' ");

            if (fileIds != null) {
                sql.append("    AND sub_ef.id IN (:fileIds) ");
            }

            if (sheetNames != null) {
                sql.append("    AND LOWER(sub_es.sheet_name) IN (:sheetNamesLower) ");
            }

            sql.append("    GROUP BY sub.excel_sheet_id, sub.row_number ");
            sql.append("    HAVING ");

            // Each keyword must appear at least once in the row
            for (int i = 0; i < keywords.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append("SUM(CASE WHEN LOWER(sub.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword")
                        .append(i).append(" AS TEXT), '%')) THEN 1 ELSE 0 END) > 0");
            }
            sql.append(") ");
        }

        sql.append("ORDER BY ec.excel_sheet_id, ec.row_number, ec.column_index");

        return sql.toString();
    }

    /**
     * Build the count query with dynamic keyword count.
     * Handles NULL parameters by conditional SQL building instead of SQL-level NULL checks.
     */
    private String buildCountQuery(List<String> keywords, List<Long> fileIds, List<String> sheetNames) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT COUNT(*) FROM excel_cells ec ");
        sql.append("JOIN excel_sheets es ON ec.excel_sheet_id = es.id ");
        sql.append("JOIN excel_files ef ON es.excel_file_id = ef.id ");
        sql.append("WHERE ef.status = 'ACTIVE' ");

        // Only add fileIds filter if provided (use IN clause for multiple values)
        if (fileIds != null) {
            sql.append("AND ef.id IN (:fileIds) ");
        }

        // Only add sheetNames filter if provided (use IN clause for multiple values, case-insensitive)
        if (sheetNames != null) {
            sql.append("AND LOWER(es.sheet_name) IN (:sheetNamesLower) ");
        }

        if (keywords.size() == 1) {
            sql.append("AND LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword0 AS TEXT), '%')) ");
        } else {
            sql.append("AND (");
            for (int i = 0; i < keywords.size(); i++) {
                if (i > 0) {
                    sql.append(" OR ");
                }
                sql.append("LOWER(ec.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword").append(i).append(" AS TEXT), '%'))");
            }
            sql.append(") ");

            sql.append("AND (ec.excel_sheet_id, ec.row_number) IN (");
            sql.append("    SELECT sub.excel_sheet_id, sub.row_number ");
            sql.append("    FROM excel_cells sub ");
            sql.append("    JOIN excel_sheets sub_es ON sub.excel_sheet_id = sub_es.id ");
            sql.append("    JOIN excel_files sub_ef ON sub_es.excel_file_id = sub_ef.id ");
            sql.append("    WHERE sub_ef.status = 'ACTIVE' ");

            if (fileIds != null) {
                sql.append("    AND sub_ef.id IN (:fileIds) ");
            }

            if (sheetNames != null) {
                sql.append("    AND LOWER(sub_es.sheet_name) IN (:sheetNamesLower) ");
            }

            sql.append("    GROUP BY sub.excel_sheet_id, sub.row_number ");
            sql.append("    HAVING ");

            for (int i = 0; i < keywords.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append("SUM(CASE WHEN LOWER(sub.cell_value_lower) LIKE LOWER(CONCAT('%', CAST(:keyword")
                        .append(i).append(" AS TEXT), '%')) THEN 1 ELSE 0 END) > 0");
            }
            sql.append(") ");
        }

        return sql.toString();
    }

    /**
     * Set query parameters for both main and count queries.
     * Only sets parameters that are actually used in the query (non-null filters).
     */
    private void setQueryParameters(NativeQuery<?> query, List<Long> fileIds, List<String> sheetNames, List<String> keywords) {
        // Only set fileIds if they're used in the query
        if (fileIds != null) {
            query.setParameterList("fileIds", fileIds);
        }

        // Only set sheetNames if they're used in the query (convert to lowercase for case-insensitive matching)
        if (sheetNames != null) {
            List<String> lowerSheetNames = sheetNames.stream()
                    .map(String::toLowerCase)
                    .toList();
            query.setParameterList("sheetNamesLower", lowerSheetNames);
        }

        // Set keywords - always present
        for (int i = 0; i < keywords.size(); i++) {
            query.setParameter("keyword" + i, keywords.get(i));
        }
    }
}
