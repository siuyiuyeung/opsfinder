package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.TechMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TechMessage entity.
 */
@Repository
public interface TechMessageRepository extends JpaRepository<TechMessage, Long> {

    /**
     * Find tech messages by category.
     *
     * @param category the category name
     * @param pageable pagination parameters
     * @return page of tech messages
     */
    Page<TechMessage> findByCategory(String category, Pageable pageable);

    /**
     * Find tech messages by severity.
     *
     * @param severity the severity level
     * @param pageable pagination parameters
     * @return page of tech messages
     */
    Page<TechMessage> findBySeverity(TechMessage.Severity severity, Pageable pageable);

    /**
     * Find tech messages by category and severity.
     *
     * @param category the category name
     * @param severity the severity level
     * @param pageable pagination parameters
     * @return page of tech messages
     */
    Page<TechMessage> findByCategoryAndSeverity(String category, TechMessage.Severity severity, Pageable pageable);

    /**
     * Find tech message by ID with action levels eagerly loaded.
     *
     * @param id the tech message ID
     * @return optional tech message with action levels
     */
    @EntityGraph(attributePaths = {"actionLevels"})
    @Query("SELECT tm FROM TechMessage tm WHERE tm.id = :id")
    Optional<TechMessage> findByIdWithActionLevels(Long id);

    /**
     * Find all tech messages with action levels eagerly loaded.
     *
     * @return list of tech messages with action levels
     */
    @EntityGraph(attributePaths = {"actionLevels"})
    @Query("SELECT tm FROM TechMessage tm")
    List<TechMessage> findAllWithActionLevels();

    /**
     * Get all distinct categories.
     *
     * @return list of unique category names
     */
    @Query("SELECT DISTINCT tm.category FROM TechMessage tm ORDER BY tm.category")
    List<String> findDistinctCategories();

    /**
     * Count tech messages by category.
     *
     * @param category the category name
     * @return number of tech messages in the category
     */
    long countByCategory(String category);

    /**
     * Count tech messages by severity.
     *
     * @param severity the severity level
     * @return number of tech messages with the specified severity
     */
    long countBySeverity(TechMessage.Severity severity);
}
