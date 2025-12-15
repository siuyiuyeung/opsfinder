package com.igsl.opsfinder.util;

import com.igsl.opsfinder.entity.ActionLevel;
import com.igsl.opsfinder.repository.ActionLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Utility class for analyzing message occurrence frequency and determining appropriate actions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FrequencyAnalyzer {

    private final ActionLevelRepository actionLevelRepository;

    /**
     * Determine the appropriate action level based on occurrence count.
     * Returns the highest priority action level that matches the occurrence count.
     *
     * @param techMessageId the tech message ID
     * @param occurrenceCount the number of times the message has occurred
     * @return optional action level, empty if no matching level found
     */
    public Optional<ActionLevel> determineActionLevel(Long techMessageId, int occurrenceCount) {
        log.debug("Determining action level for tech message ID {} with {} occurrences",
                techMessageId, occurrenceCount);

        List<ActionLevel> matchingLevels = actionLevelRepository
                .findByTechMessageIdAndOccurrenceRange(techMessageId, occurrenceCount);

        if (matchingLevels.isEmpty()) {
            log.warn("No action level found for tech message ID {} with {} occurrences",
                    techMessageId, occurrenceCount);
            return Optional.empty();
        }

        // Return the highest priority level (first in the list, already sorted by priority DESC)
        ActionLevel selectedLevel = matchingLevels.get(0);
        log.debug("Selected action level ID {} (priority {}) for {} occurrences",
                selectedLevel.getId(), selectedLevel.getPriority(), occurrenceCount);

        return Optional.of(selectedLevel);
    }

    /**
     * Get all matching action levels for an occurrence count.
     * Useful for showing all possible actions.
     *
     * @param techMessageId the tech message ID
     * @param occurrenceCount the number of times the message has occurred
     * @return list of matching action levels, ordered by priority descending
     */
    public List<ActionLevel> getAllMatchingActionLevels(Long techMessageId, int occurrenceCount) {
        log.debug("Getting all matching action levels for tech message ID {} with {} occurrences",
                techMessageId, occurrenceCount);

        return actionLevelRepository.findByTechMessageIdAndOccurrenceRange(techMessageId, occurrenceCount);
    }

    /**
     * Check if an action level exists for a specific occurrence count.
     *
     * @param techMessageId the tech message ID
     * @param occurrenceCount the number of occurrences
     * @return true if at least one action level matches
     */
    public boolean hasActionLevel(Long techMessageId, int occurrenceCount) {
        List<ActionLevel> levels = actionLevelRepository
                .findByTechMessageIdAndOccurrenceRange(techMessageId, occurrenceCount);
        return !levels.isEmpty();
    }

    /**
     * Get the recommended action text for a specific occurrence count.
     *
     * @param techMessageId the tech message ID
     * @param occurrenceCount the number of occurrences
     * @return optional action text, empty if no matching level found
     */
    public Optional<String> getRecommendedAction(Long techMessageId, int occurrenceCount) {
        return determineActionLevel(techMessageId, occurrenceCount)
                .map(ActionLevel::getActionText);
    }
}
