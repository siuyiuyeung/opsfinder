package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.request.ActionLevelRequest;
import com.igsl.opsfinder.dto.request.TechMessageRequest;
import com.igsl.opsfinder.dto.response.ActionLevelResponse;
import com.igsl.opsfinder.dto.response.TechMessageResponse;
import com.igsl.opsfinder.dto.response.PatternMatchResponse;
import com.igsl.opsfinder.entity.ActionLevel;
import com.igsl.opsfinder.entity.TechMessage;
import com.igsl.opsfinder.exception.BadRequestException;
import com.igsl.opsfinder.exception.ResourceNotFoundException;
import com.igsl.opsfinder.mapper.TechMessageMapper;
import com.igsl.opsfinder.repository.ActionLevelRepository;
import com.igsl.opsfinder.repository.TechMessageRepository;
import com.igsl.opsfinder.util.PatternMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for tech message management with pattern matching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TechMessageService {

    private final TechMessageRepository techMessageRepository;
    private final ActionLevelRepository actionLevelRepository;
    private final TechMessageMapper techMessageMapper;
    private final PatternMatcher patternMatcher;

    /**
     * Get all tech messages with pagination.
     *
     * @param pageable pagination and sorting parameters
     * @return page of tech message responses
     */
    public Page<TechMessageResponse> getAllTechMessages(Pageable pageable) {
        log.debug("Fetching all tech messages with pagination: {}", pageable);
        Page<TechMessage> techMessages = techMessageRepository.findAll(pageable);
        return techMessages.map(techMessageMapper::toResponse);
    }

    /**
     * Get tech message by ID with action levels.
     *
     * @param id the tech message ID
     * @return tech message response
     * @throws ResourceNotFoundException if tech message not found
     */
    public TechMessageResponse getTechMessageById(Long id) {
        log.debug("Fetching tech message by ID: {}", id);
        TechMessage techMessage = techMessageRepository.findByIdWithActionLevels(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tech message not found with ID: " + id));
        return techMessageMapper.toResponse(techMessage);
    }

    /**
     * Get tech messages by category.
     *
     * @param category the category name
     * @param pageable pagination parameters
     * @return page of tech message responses
     */
    public Page<TechMessageResponse> getTechMessagesByCategory(String category, Pageable pageable) {
        log.debug("Fetching tech messages by category: {}", category);
        Page<TechMessage> techMessages = techMessageRepository.findByCategory(category, pageable);
        return techMessages.map(techMessageMapper::toResponse);
    }

    /**
     * Get tech messages by severity.
     *
     * @param severity the severity level
     * @param pageable pagination parameters
     * @return page of tech message responses
     */
    public Page<TechMessageResponse> getTechMessagesBySeverity(TechMessage.Severity severity, Pageable pageable) {
        log.debug("Fetching tech messages by severity: {}", severity);
        Page<TechMessage> techMessages = techMessageRepository.findBySeverity(severity, pageable);
        return techMessages.map(techMessageMapper::toResponse);
    }

    /**
     * Get tech messages by category and severity.
     *
     * @param category the category name
     * @param severity the severity level
     * @param pageable pagination parameters
     * @return page of tech message responses
     */
    public Page<TechMessageResponse> getTechMessagesByCategoryAndSeverity(
            String category, TechMessage.Severity severity, Pageable pageable) {
        log.debug("Fetching tech messages by category: {} and severity: {}", category, severity);
        Page<TechMessage> techMessages = techMessageRepository.findByCategoryAndSeverity(category, severity, pageable);
        return techMessages.map(techMessageMapper::toResponse);
    }

    /**
     * Get all distinct categories.
     *
     * @return list of unique category names
     */
    public List<String> getDistinctCategories() {
        log.debug("Fetching distinct categories");
        return techMessageRepository.findDistinctCategories();
    }

    /**
     * Create a new tech message.
     *
     * @param request tech message creation request
     * @return created tech message response
     * @throws BadRequestException if regex pattern is invalid
     */
    @Transactional
    public TechMessageResponse createTechMessage(TechMessageRequest request) {
        log.info("Creating new tech message with category: {} and severity: {}",
                request.getCategory(), request.getSeverity());

        // Validate regex pattern
        PatternMatcher.ValidationResult validation = patternMatcher.validatePattern(request.getPattern());
        if (!validation.isValid()) {
            throw new BadRequestException("Invalid regex pattern: " + validation.getErrorMessage());
        }

        TechMessage techMessage = techMessageMapper.toEntity(request);
        TechMessage savedTechMessage = techMessageRepository.save(techMessage);

        log.info("Tech message created successfully with ID: {}", savedTechMessage.getId());
        return techMessageMapper.toResponse(savedTechMessage);
    }

    /**
     * Update an existing tech message.
     *
     * @param id the tech message ID
     * @param request tech message update request
     * @return updated tech message response
     * @throws ResourceNotFoundException if tech message not found
     * @throws BadRequestException if regex pattern is invalid
     */
    @Transactional
    public TechMessageResponse updateTechMessage(Long id, TechMessageRequest request) {
        log.info("Updating tech message with ID: {}", id);

        // Validate regex pattern if provided
        if (request.getPattern() != null) {
            PatternMatcher.ValidationResult validation = patternMatcher.validatePattern(request.getPattern());
            if (!validation.isValid()) {
                throw new BadRequestException("Invalid regex pattern: " + validation.getErrorMessage());
            }
        }

        TechMessage techMessage = techMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tech message not found with ID: " + id));

        techMessageMapper.updateEntityFromRequest(request, techMessage);
        TechMessage updatedTechMessage = techMessageRepository.save(techMessage);

        log.info("Tech message updated successfully with ID: {}", id);
        return techMessageMapper.toResponse(updatedTechMessage);
    }

    /**
     * Delete a tech message by ID.
     *
     * @param id the tech message ID
     * @throws ResourceNotFoundException if tech message not found
     */
    @Transactional
    public void deleteTechMessage(Long id) {
        log.info("Deleting tech message with ID: {}", id);

        if (!techMessageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tech message not found with ID: " + id);
        }

        techMessageRepository.deleteById(id);
        log.info("Tech message deleted successfully with ID: {}", id);
    }

    /**
     * Add an action level to a tech message.
     *
     * @param techMessageId the tech message ID
     * @param request action level request
     * @return created action level response
     * @throws ResourceNotFoundException if tech message not found
     */
    @Transactional
    public ActionLevelResponse addActionLevel(Long techMessageId, ActionLevelRequest request) {
        log.info("Adding action level to tech message ID: {}", techMessageId);

        TechMessage techMessage = techMessageRepository.findById(techMessageId)
                .orElseThrow(() -> new ResourceNotFoundException("Tech message not found with ID: " + techMessageId));

        ActionLevel actionLevel = techMessageMapper.toActionLevelEntity(request);
        techMessage.addActionLevel(actionLevel);

        TechMessage savedTechMessage = techMessageRepository.save(techMessage);
        ActionLevel savedActionLevel = savedTechMessage.getActionLevels()
                .stream()
                .filter(al -> al.getOccurrenceMin().equals(request.getOccurrenceMin())
                        && al.getPriority().equals(request.getPriority()))
                .findFirst()
                .orElse(actionLevel);

        log.info("Action level added successfully with ID: {}", savedActionLevel.getId());
        return techMessageMapper.toActionLevelResponse(savedActionLevel);
    }

    /**
     * Update an action level.
     *
     * @param actionLevelId the action level ID
     * @param request action level update request
     * @return updated action level response
     * @throws ResourceNotFoundException if action level not found
     */
    @Transactional
    public ActionLevelResponse updateActionLevel(Long actionLevelId, ActionLevelRequest request) {
        log.info("Updating action level with ID: {}", actionLevelId);

        ActionLevel actionLevel = actionLevelRepository.findById(actionLevelId)
                .orElseThrow(() -> new ResourceNotFoundException("Action level not found with ID: " + actionLevelId));

        techMessageMapper.updateActionLevelFromRequest(request, actionLevel);
        ActionLevel updatedActionLevel = actionLevelRepository.save(actionLevel);

        log.info("Action level updated successfully with ID: {}", actionLevelId);
        return techMessageMapper.toActionLevelResponse(updatedActionLevel);
    }

    /**
     * Delete an action level.
     *
     * @param actionLevelId the action level ID
     * @throws ResourceNotFoundException if action level not found
     */
    @Transactional
    public void deleteActionLevel(Long actionLevelId) {
        log.info("Deleting action level with ID: {}", actionLevelId);

        if (!actionLevelRepository.existsById(actionLevelId)) {
            throw new ResourceNotFoundException("Action level not found with ID: " + actionLevelId);
        }

        actionLevelRepository.deleteById(actionLevelId);
        log.info("Action level deleted successfully with ID: {}", actionLevelId);
    }

    /**
     * Match text against all tech message patterns.
     *
     * @param errorText the text to match
     * @return pattern match response with matched tech message and recommended action
     */
    public PatternMatchResponse matchText(String errorText) {
        log.debug("Matching text against patterns: {}", errorText);

        List<TechMessage> allPatterns = techMessageRepository.findAllWithActionLevels();
        Optional<PatternMatcher.MatchResult> matchResult = patternMatcher.matchMessage(errorText, allPatterns);

        if (matchResult.isEmpty()) {
            log.debug("No pattern matched for text");
            return PatternMatchResponse.builder()
                    .matched(false)
                    .build();
        }

        PatternMatcher.MatchResult result = matchResult.get();
        TechMessage matched = result.getTechMessage();

        // For demonstration, assuming occurrence count of 1 for first match
        // In real usage, this would be passed as a parameter or calculated from incident history
        int occurrenceCount = 1;
        String recommendedAction = getRecommendedActionForOccurrence(matched.getId(), occurrenceCount);

        return PatternMatchResponse.builder()
                .matched(true)
                .techMessage(techMessageMapper.toResponse(matched))
                .matchedText(result.getMatchedText())
                .variables(result.getVariables())
                .recommendedAction(recommendedAction)
                .occurrenceCount(occurrenceCount)
                .build();
    }

    /**
     * Get recommended action for a specific occurrence count.
     *
     * @param techMessageId the tech message ID
     * @param occurrenceCount the number of occurrences
     * @return recommended action text, or default message if not found
     */
    private String getRecommendedActionForOccurrence(Long techMessageId, int occurrenceCount) {
        List<ActionLevel> levels = actionLevelRepository
                .findByTechMessageIdAndOccurrenceRange(techMessageId, occurrenceCount);

        if (levels.isEmpty()) {
            return "No specific action defined for this occurrence count.";
        }

        return levels.get(0).getActionText();
    }

    /**
     * Get message count by category.
     *
     * @param category the category name
     * @return number of tech messages in the category
     */
    public long countMessagesByCategory(String category) {
        return techMessageRepository.countByCategory(category);
    }

    /**
     * Get message count by severity.
     *
     * @param severity the severity level
     * @return number of tech messages with the specified severity
     */
    public long countMessagesBySeverity(TechMessage.Severity severity) {
        return techMessageRepository.countBySeverity(severity);
    }
}
