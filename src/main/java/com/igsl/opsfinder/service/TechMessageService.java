package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.TechMessageSearchRequest;
import com.igsl.opsfinder.dto.TechMessageSearchResponse;
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

import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Search tech messages using fuzzy keyword matching and/or exact pattern matching.
     * Supports hybrid search with ranking by relevance.
     *
     * @param request search request containing search text, occurrence count, and match mode
     * @return search response with matched tech messages and recommended actions
     */
    public TechMessageSearchResponse searchTechMessages(TechMessageSearchRequest request) {
        log.info("Searching tech messages with text: '{}', mode: {}, occurrenceCount: {}",
                request.getSearchText(), request.getMatchMode(), request.getOccurrenceCount());

        if (request.getSearchText() == null || request.getSearchText().trim().isEmpty()) {
            return TechMessageSearchResponse.builder()
                    .matches(Collections.emptyList())
                    .noMatches(true)
                    .build();
        }

        String searchText = request.getSearchText().trim();
        List<TechMessageSearchResponse.SearchMatch> allMatches = new ArrayList<>();
        Set<Long> matchedIds = new HashSet<>();  // For deduplication

        // 1. Exact pattern matching (if mode is EXACT or BOTH)
        if (request.getMatchMode() == TechMessageSearchRequest.MatchMode.EXACT ||
                request.getMatchMode() == TechMessageSearchRequest.MatchMode.BOTH) {

            List<TechMessage> allPatterns = techMessageRepository.findAllWithActionLevels();
            Optional<PatternMatcher.MatchResult> exactMatch = patternMatcher.matchMessage(searchText, allPatterns);

            if (exactMatch.isPresent()) {
                PatternMatcher.MatchResult result = exactMatch.get();
                TechMessage matched = result.getTechMessage();
                matchedIds.add(matched.getId());

                ActionLevel recommendedAction = determineRecommendedAction(
                        matched, request.getOccurrenceCount());

                allMatches.add(TechMessageSearchResponse.SearchMatch.builder()
                        .techMessage(techMessageMapper.toResponse(matched))
                        .matchType(TechMessageSearchResponse.MatchType.EXACT)
                        .matchScore(1.0)  // Exact matches have highest score
                        .matchedText(result.getMatchedText())
                        .extractedVariables(result.getVariables())
                        .recommendedAction(recommendedAction != null ? techMessageMapper.toActionLevelResponse(recommendedAction) : null)
                        .allActionLevels(matched.getActionLevels().stream()
                                .map(techMessageMapper::toActionLevelResponse)
                                .collect(Collectors.toList()))
                        .build());

                log.debug("Exact pattern match found: {}", matched.getCategory());
            }
        }

        // 2. Fuzzy keyword search (if mode is FUZZY or BOTH)
        if (request.getMatchMode() == TechMessageSearchRequest.MatchMode.FUZZY ||
                request.getMatchMode() == TechMessageSearchRequest.MatchMode.BOTH) {

            List<TechMessage> fuzzyMatches = performFuzzySearch(searchText);

            for (TechMessage techMessage : fuzzyMatches) {
                // Skip if already matched by exact pattern
                if (matchedIds.contains(techMessage.getId())) {
                    continue;
                }

                double score = calculateFuzzyMatchScore(techMessage, searchText);
                ActionLevel recommendedAction = determineRecommendedAction(
                        techMessage, request.getOccurrenceCount());

                allMatches.add(TechMessageSearchResponse.SearchMatch.builder()
                        .techMessage(techMessageMapper.toResponse(techMessage))
                        .matchType(TechMessageSearchResponse.MatchType.FUZZY)
                        .matchScore(score)
                        .matchedText(null)
                        .extractedVariables(null)
                        .recommendedAction(recommendedAction != null ? techMessageMapper.toActionLevelResponse(recommendedAction) : null)
                        .allActionLevels(techMessage.getActionLevels().stream()
                                .map(techMessageMapper::toActionLevelResponse)
                                .collect(Collectors.toList()))
                        .build());

                log.debug("Fuzzy match found: {} (score: {})", techMessage.getCategory(), score);
            }
        }

        // 3. Sort by match type (exact first) and score (highest first)
        allMatches.sort((a, b) -> {
            // Exact matches always come first
            if (a.getMatchType() != b.getMatchType()) {
                return a.getMatchType() == TechMessageSearchResponse.MatchType.EXACT ? -1 : 1;
            }
            // Then sort by score descending
            return Double.compare(b.getMatchScore(), a.getMatchScore());
        });

        log.info("Search completed. Found {} matches", allMatches.size());

        return TechMessageSearchResponse.builder()
                .matches(allMatches)
                .noMatches(allMatches.isEmpty())
                .build();
    }

    /**
     * Perform fuzzy keyword search across category, description, and pattern.
     *
     * @param searchText the search text (can be multiple keywords)
     * @return list of matching tech messages
     */
    private List<TechMessage> performFuzzySearch(String searchText) {
        // Split search text into keywords (max 3 keywords for performance)
        String[] keywords = searchText.toLowerCase().split("\\s+");

        if (keywords.length == 1) {
            return techMessageRepository.fuzzySearchByKeyword(keywords[0]);
        } else if (keywords.length == 2) {
            return techMessageRepository.fuzzySearchByMultipleKeywords(keywords[0], keywords[1], null);
        } else if (keywords.length >= 3) {
            return techMessageRepository.fuzzySearchByMultipleKeywords(keywords[0], keywords[1], keywords[2]);
        }

        return Collections.emptyList();
    }

    /**
     * Calculate fuzzy match score based on how well the tech message matches the search text.
     * Score ranges from 0.0 to 0.9 (exact pattern matches get 1.0).
     *
     * @param techMessage the tech message
     * @param searchText the search text
     * @return match score (higher is better)
     */
    private double calculateFuzzyMatchScore(TechMessage techMessage, String searchText) {
        String lowerSearchText = searchText.toLowerCase();
        double score = 0.0;

        // Category exact match: +0.5
        if (techMessage.getCategory().toLowerCase().equals(lowerSearchText)) {
            score += 0.5;
        }
        // Category contains: +0.3
        else if (techMessage.getCategory().toLowerCase().contains(lowerSearchText)) {
            score += 0.3;
        }

        // Description contains: +0.2
        if (techMessage.getDescription() != null &&
                techMessage.getDescription().toLowerCase().contains(lowerSearchText)) {
            score += 0.2;
        }

        // Pattern contains: +0.2
        if (techMessage.getPattern().toLowerCase().contains(lowerSearchText)) {
            score += 0.2;
        }

        // Severity bonus: CRITICAL +0.1, HIGH +0.075, MEDIUM +0.05, LOW +0.025
        switch (techMessage.getSeverity()) {
            case CRITICAL -> score += 0.1;
            case HIGH -> score += 0.075;
            case MEDIUM -> score += 0.05;
            case LOW -> score += 0.025;
        }

        return Math.min(score, 0.9);  // Cap at 0.9 to ensure exact matches are always higher
    }

    /**
     * Determine recommended action based on occurrence count.
     * If occurrence count is not provided, return null.
     *
     * @param techMessage the tech message
     * @param occurrenceCount the occurrence count (can be null)
     * @return recommended action level, or null if not applicable
     */
    private ActionLevel determineRecommendedAction(TechMessage techMessage, Integer occurrenceCount) {
        if (occurrenceCount == null || occurrenceCount <= 0) {
            return null;
        }

        List<ActionLevel> levels = actionLevelRepository
                .findByTechMessageIdAndOccurrenceRange(techMessage.getId(), occurrenceCount);

        return levels.isEmpty() ? null : levels.get(0);
    }
}
