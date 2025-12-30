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
            Long fileId,
            String sheetName,
            List<String> keywords,
            Pageable pageable) {

        if (keywords == null || keywords.isEmpty()) {
            return Page.empty(pageable);
        }

        // Build the SQL query dynamically (conditionally includes filters based on NULL checks)
        String sql = buildSearchQuery(keywords, fileId, sheetName);
        String countSql = buildCountQuery(keywords, fileId, sheetName);

        // Create and configure the query using Hibernate NativeQuery for better type handling
        NativeQuery<ExcelCell> query = (NativeQuery<ExcelCell>) entityManager.createNativeQuery(sql, ExcelCell.class);
        NativeQuery<Long> countQuery = (NativeQuery<Long>) entityManager.createNativeQuery(countSql);

        // Set parameters - only set non-null parameters since SQL is built conditionally
        setQueryParameters(query, fileId, sheetName, keywords);
        setQueryParameters(countQuery, fileId, sheetName, keywords);

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
    private String buildSearchQuery(List<String> keywords, Long fileId, String sheetName) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ec.* FROM excel_cells ec ");
        sql.append("JOIN excel_sheets es ON ec.excel_sheet_id = es.id ");
        sql.append("JOIN excel_files ef ON es.excel_file_id = ef.id ");
        sql.append("WHERE ef.status = 'ACTIVE' ");

        // Only add fileId filter if provided
        if (fileId != null) {
            sql.append("AND ef.id = :fileId ");
        }

        // Only add sheetName filter if provided
        if (sheetName != null) {
            sql.append("AND LOWER(es.sheet_name) = LOWER(CAST(:sheetName AS TEXT)) ");
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

            if (fileId != null) {
                sql.append("    AND sub_ef.id = :fileId ");
            }

            if (sheetName != null) {
                sql.append("    AND LOWER(sub_es.sheet_name) = LOWER(CAST(:sheetName AS TEXT)) ");
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
    private String buildCountQuery(List<String> keywords, Long fileId, String sheetName) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT COUNT(*) FROM excel_cells ec ");
        sql.append("JOIN excel_sheets es ON ec.excel_sheet_id = es.id ");
        sql.append("JOIN excel_files ef ON es.excel_file_id = ef.id ");
        sql.append("WHERE ef.status = 'ACTIVE' ");

        // Only add fileId filter if provided
        if (fileId != null) {
            sql.append("AND ef.id = :fileId ");
        }

        // Only add sheetName filter if provided
        if (sheetName != null) {
            sql.append("AND LOWER(es.sheet_name) = LOWER(CAST(:sheetName AS TEXT)) ");
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

            if (fileId != null) {
                sql.append("    AND sub_ef.id = :fileId ");
            }

            if (sheetName != null) {
                sql.append("    AND LOWER(sub_es.sheet_name) = LOWER(CAST(:sheetName AS TEXT)) ");
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
    private void setQueryParameters(NativeQuery<?> query, Long fileId, String sheetName, List<String> keywords) {
        // Only set fileId if it's used in the query
        if (fileId != null) {
            query.setParameter("fileId", fileId);
        }

        // Only set sheetName if it's used in the query
        if (sheetName != null) {
            query.setParameter("sheetName", sheetName);
        }

        // Set keywords - always present
        for (int i = 0; i < keywords.size(); i++) {
            query.setParameter("keyword" + i, keywords.get(i));
        }
    }
}
