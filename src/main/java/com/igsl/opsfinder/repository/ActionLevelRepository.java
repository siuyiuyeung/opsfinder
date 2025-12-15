package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.ActionLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ActionLevel entity.
 */
@Repository
public interface ActionLevelRepository extends JpaRepository<ActionLevel, Long> {

    /**
     * Find action levels by tech message ID.
     *
     * @param techMessageId the tech message ID
     * @return list of action levels
     */
    List<ActionLevel> findByTechMessageId(Long techMessageId);

    /**
     * Find action levels that apply for a specific occurrence count.
     *
     * @param techMessageId the tech message ID
     * @param occurrenceCount the number of occurrences
     * @return list of matching action levels, ordered by priority descending
     */
    @Query("""
            SELECT al FROM ActionLevel al
            WHERE al.techMessage.id = :techMessageId
            AND al.occurrenceMin <= :occurrenceCount
            AND (al.occurrenceMax IS NULL OR al.occurrenceMax >= :occurrenceCount)
            ORDER BY al.priority DESC, al.occurrenceMin DESC
            """)
    List<ActionLevel> findByTechMessageIdAndOccurrenceRange(
            @Param("techMessageId") Long techMessageId,
            @Param("occurrenceCount") int occurrenceCount
    );

    /**
     * Find the highest priority action level for a specific occurrence count.
     *
     * @param techMessageId the tech message ID
     * @param occurrenceCount the number of occurrences
     * @return list with the top matching action level (limited to 1)
     */
    @Query(value = """
            SELECT al FROM ActionLevel al
            WHERE al.techMessage.id = :techMessageId
            AND al.occurrenceMin <= :occurrenceCount
            AND (al.occurrenceMax IS NULL OR al.occurrenceMax >= :occurrenceCount)
            ORDER BY al.priority DESC, al.occurrenceMin DESC
            LIMIT 1
            """)
    List<ActionLevel> findTopPriorityActionLevel(
            @Param("techMessageId") Long techMessageId,
            @Param("occurrenceCount") int occurrenceCount
    );

    /**
     * Delete all action levels for a specific tech message.
     *
     * @param techMessageId the tech message ID
     */
    void deleteByTechMessageId(Long techMessageId);
}
