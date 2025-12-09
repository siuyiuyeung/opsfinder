package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.ErrorMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ErrorMessage entity.
 */
@Repository
public interface ErrorMessageRepository extends JpaRepository<ErrorMessage, Long> {

    /**
     * Find error messages by category.
     *
     * @param category the category name
     * @param pageable pagination parameters
     * @return page of error messages
     */
    Page<ErrorMessage> findByCategory(String category, Pageable pageable);

    /**
     * Find error messages by severity.
     *
     * @param severity the severity level
     * @param pageable pagination parameters
     * @return page of error messages
     */
    Page<ErrorMessage> findBySeverity(ErrorMessage.Severity severity, Pageable pageable);

    /**
     * Find error messages by category and severity.
     *
     * @param category the category name
     * @param severity the severity level
     * @param pageable pagination parameters
     * @return page of error messages
     */
    Page<ErrorMessage> findByCategoryAndSeverity(String category, ErrorMessage.Severity severity, Pageable pageable);

    /**
     * Find error message by ID with action levels eagerly loaded.
     *
     * @param id the error message ID
     * @return optional error message with action levels
     */
    @EntityGraph(attributePaths = {"actionLevels"})
    @Query("SELECT em FROM ErrorMessage em WHERE em.id = :id")
    Optional<ErrorMessage> findByIdWithActionLevels(Long id);

    /**
     * Find all error messages with action levels eagerly loaded.
     *
     * @return list of error messages with action levels
     */
    @EntityGraph(attributePaths = {"actionLevels"})
    @Query("SELECT em FROM ErrorMessage em")
    List<ErrorMessage> findAllWithActionLevels();

    /**
     * Get all distinct categories.
     *
     * @return list of unique category names
     */
    @Query("SELECT DISTINCT em.category FROM ErrorMessage em ORDER BY em.category")
    List<String> findDistinctCategories();

    /**
     * Count error messages by category.
     *
     * @param category the category name
     * @return number of error messages in the category
     */
    long countByCategory(String category);

    /**
     * Count error messages by severity.
     *
     * @param severity the severity level
     * @return number of error messages with the specified severity
     */
    long countBySeverity(ErrorMessage.Severity severity);
}
