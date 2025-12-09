package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.request.ActionLevelRequest;
import com.igsl.opsfinder.dto.request.ErrorMessageRequest;
import com.igsl.opsfinder.dto.response.ActionLevelResponse;
import com.igsl.opsfinder.dto.response.ErrorMessageResponse;
import com.igsl.opsfinder.dto.response.PatternMatchResponse;
import com.igsl.opsfinder.entity.ActionLevel;
import com.igsl.opsfinder.entity.ErrorMessage;
import com.igsl.opsfinder.exception.BadRequestException;
import com.igsl.opsfinder.exception.ResourceNotFoundException;
import com.igsl.opsfinder.mapper.ErrorMessageMapper;
import com.igsl.opsfinder.repository.ActionLevelRepository;
import com.igsl.opsfinder.repository.ErrorMessageRepository;
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
 * Service layer for error message management with pattern matching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ErrorMessageService {

    private final ErrorMessageRepository errorMessageRepository;
    private final ActionLevelRepository actionLevelRepository;
    private final ErrorMessageMapper errorMessageMapper;
    private final PatternMatcher patternMatcher;

    /**
     * Get all error messages with pagination.
     *
     * @param pageable pagination and sorting parameters
     * @return page of error message responses
     */
    public Page<ErrorMessageResponse> getAllErrorMessages(Pageable pageable) {
        log.debug("Fetching all error messages with pagination: {}", pageable);
        Page<ErrorMessage> errorMessages = errorMessageRepository.findAll(pageable);
        return errorMessages.map(errorMessageMapper::toResponse);
    }

    /**
     * Get error message by ID with action levels.
     *
     * @param id the error message ID
     * @return error message response
     * @throws ResourceNotFoundException if error message not found
     */
    public ErrorMessageResponse getErrorMessageById(Long id) {
        log.debug("Fetching error message by ID: {}", id);
        ErrorMessage errorMessage = errorMessageRepository.findByIdWithActionLevels(id)
                .orElseThrow(() -> new ResourceNotFoundException("Error message not found with ID: " + id));
        return errorMessageMapper.toResponse(errorMessage);
    }

    /**
     * Get error messages by category.
     *
     * @param category the category name
     * @param pageable pagination parameters
     * @return page of error message responses
     */
    public Page<ErrorMessageResponse> getErrorMessagesByCategory(String category, Pageable pageable) {
        log.debug("Fetching error messages by category: {}", category);
        Page<ErrorMessage> errorMessages = errorMessageRepository.findByCategory(category, pageable);
        return errorMessages.map(errorMessageMapper::toResponse);
    }

    /**
     * Get error messages by severity.
     *
     * @param severity the severity level
     * @param pageable pagination parameters
     * @return page of error message responses
     */
    public Page<ErrorMessageResponse> getErrorMessagesBySeverity(ErrorMessage.Severity severity, Pageable pageable) {
        log.debug("Fetching error messages by severity: {}", severity);
        Page<ErrorMessage> errorMessages = errorMessageRepository.findBySeverity(severity, pageable);
        return errorMessages.map(errorMessageMapper::toResponse);
    }

    /**
     * Get error messages by category and severity.
     *
     * @param category the category name
     * @param severity the severity level
     * @param pageable pagination parameters
     * @return page of error message responses
     */
    public Page<ErrorMessageResponse> getErrorMessagesByCategoryAndSeverity(
            String category, ErrorMessage.Severity severity, Pageable pageable) {
        log.debug("Fetching error messages by category: {} and severity: {}", category, severity);
        Page<ErrorMessage> errorMessages = errorMessageRepository.findByCategoryAndSeverity(category, severity, pageable);
        return errorMessages.map(errorMessageMapper::toResponse);
    }

    /**
     * Get all distinct categories.
     *
     * @return list of unique category names
     */
    public List<String> getDistinctCategories() {
        log.debug("Fetching distinct categories");
        return errorMessageRepository.findDistinctCategories();
    }

    /**
     * Create a new error message.
     *
     * @param request error message creation request
     * @return created error message response
     * @throws BadRequestException if regex pattern is invalid
     */
    @Transactional
    public ErrorMessageResponse createErrorMessage(ErrorMessageRequest request) {
        log.info("Creating new error message with category: {} and severity: {}",
                request.getCategory(), request.getSeverity());

        // Validate regex pattern
        PatternMatcher.ValidationResult validation = patternMatcher.validatePattern(request.getPattern());
        if (!validation.isValid()) {
            throw new BadRequestException("Invalid regex pattern: " + validation.getErrorMessage());
        }

        ErrorMessage errorMessage = errorMessageMapper.toEntity(request);
        ErrorMessage savedErrorMessage = errorMessageRepository.save(errorMessage);

        log.info("Error message created successfully with ID: {}", savedErrorMessage.getId());
        return errorMessageMapper.toResponse(savedErrorMessage);
    }

    /**
     * Update an existing error message.
     *
     * @param id the error message ID
     * @param request error message update request
     * @return updated error message response
     * @throws ResourceNotFoundException if error message not found
     * @throws BadRequestException if regex pattern is invalid
     */
    @Transactional
    public ErrorMessageResponse updateErrorMessage(Long id, ErrorMessageRequest request) {
        log.info("Updating error message with ID: {}", id);

        // Validate regex pattern if provided
        if (request.getPattern() != null) {
            PatternMatcher.ValidationResult validation = patternMatcher.validatePattern(request.getPattern());
            if (!validation.isValid()) {
                throw new BadRequestException("Invalid regex pattern: " + validation.getErrorMessage());
            }
        }

        ErrorMessage errorMessage = errorMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Error message not found with ID: " + id));

        errorMessageMapper.updateEntityFromRequest(request, errorMessage);
        ErrorMessage updatedErrorMessage = errorMessageRepository.save(errorMessage);

        log.info("Error message updated successfully with ID: {}", id);
        return errorMessageMapper.toResponse(updatedErrorMessage);
    }

    /**
     * Delete an error message by ID.
     *
     * @param id the error message ID
     * @throws ResourceNotFoundException if error message not found
     */
    @Transactional
    public void deleteErrorMessage(Long id) {
        log.info("Deleting error message with ID: {}", id);

        if (!errorMessageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Error message not found with ID: " + id);
        }

        errorMessageRepository.deleteById(id);
        log.info("Error message deleted successfully with ID: {}", id);
    }

    /**
     * Add an action level to an error message.
     *
     * @param errorMessageId the error message ID
     * @param request action level request
     * @return created action level response
     * @throws ResourceNotFoundException if error message not found
     */
    @Transactional
    public ActionLevelResponse addActionLevel(Long errorMessageId, ActionLevelRequest request) {
        log.info("Adding action level to error message ID: {}", errorMessageId);

        ErrorMessage errorMessage = errorMessageRepository.findById(errorMessageId)
                .orElseThrow(() -> new ResourceNotFoundException("Error message not found with ID: " + errorMessageId));

        ActionLevel actionLevel = errorMessageMapper.toActionLevelEntity(request);
        errorMessage.addActionLevel(actionLevel);

        ErrorMessage savedErrorMessage = errorMessageRepository.save(errorMessage);
        ActionLevel savedActionLevel = savedErrorMessage.getActionLevels()
                .stream()
                .filter(al -> al.getOccurrenceMin().equals(request.getOccurrenceMin())
                        && al.getPriority().equals(request.getPriority()))
                .findFirst()
                .orElse(actionLevel);

        log.info("Action level added successfully with ID: {}", savedActionLevel.getId());
        return errorMessageMapper.toActionLevelResponse(savedActionLevel);
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

        errorMessageMapper.updateActionLevelFromRequest(request, actionLevel);
        ActionLevel updatedActionLevel = actionLevelRepository.save(actionLevel);

        log.info("Action level updated successfully with ID: {}", actionLevelId);
        return errorMessageMapper.toActionLevelResponse(updatedActionLevel);
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
     * Match error text against all error message patterns.
     *
     * @param errorText the error text to match
     * @return pattern match response with matched error message and recommended action
     */
    public PatternMatchResponse matchErrorText(String errorText) {
        log.debug("Matching error text against patterns: {}", errorText);

        List<ErrorMessage> allPatterns = errorMessageRepository.findAllWithActionLevels();
        Optional<PatternMatcher.MatchResult> matchResult = patternMatcher.matchError(errorText, allPatterns);

        if (matchResult.isEmpty()) {
            log.debug("No pattern matched for error text");
            return PatternMatchResponse.builder()
                    .matched(false)
                    .build();
        }

        PatternMatcher.MatchResult result = matchResult.get();
        ErrorMessage matched = result.getErrorMessage();

        // For demonstration, assuming occurrence count of 1 for first match
        // In real usage, this would be passed as a parameter or calculated from incident history
        int occurrenceCount = 1;
        String recommendedAction = getRecommendedActionForOccurrence(matched.getId(), occurrenceCount);

        return PatternMatchResponse.builder()
                .matched(true)
                .errorMessage(errorMessageMapper.toResponse(matched))
                .matchedText(result.getMatchedText())
                .variables(result.getVariables())
                .recommendedAction(recommendedAction)
                .occurrenceCount(occurrenceCount)
                .build();
    }

    /**
     * Get recommended action for a specific occurrence count.
     *
     * @param errorMessageId the error message ID
     * @param occurrenceCount the number of occurrences
     * @return recommended action text, or default message if not found
     */
    private String getRecommendedActionForOccurrence(Long errorMessageId, int occurrenceCount) {
        List<ActionLevel> levels = actionLevelRepository
                .findByErrorMessageIdAndOccurrenceRange(errorMessageId, occurrenceCount);

        if (levels.isEmpty()) {
            return "No specific action defined for this occurrence count.";
        }

        return levels.get(0).getActionText();
    }

    /**
     * Get error count by category.
     *
     * @param category the category name
     * @return number of error messages in the category
     */
    public long countErrorsByCategory(String category) {
        return errorMessageRepository.countByCategory(category);
    }

    /**
     * Get error count by severity.
     *
     * @param severity the severity level
     * @return number of error messages with the specified severity
     */
    public long countErrorsBySeverity(ErrorMessage.Severity severity) {
        return errorMessageRepository.countBySeverity(severity);
    }
}
