package com.igsl.opsfinder.controller;

import com.igsl.opsfinder.dto.TechMessageSearchRequest;
import com.igsl.opsfinder.dto.TechMessageSearchResponse;
import com.igsl.opsfinder.dto.request.ActionLevelRequest;
import com.igsl.opsfinder.dto.request.TechMessageRequest;
import com.igsl.opsfinder.dto.response.ActionLevelResponse;
import com.igsl.opsfinder.dto.response.TechMessageResponse;
import com.igsl.opsfinder.dto.response.PatternMatchResponse;
import com.igsl.opsfinder.entity.TechMessage;
import com.igsl.opsfinder.service.TechMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for tech message management operations.
 * Endpoints are protected with role-based access control.
 */
@RestController
@RequestMapping("/api/tech-messages")
@RequiredArgsConstructor
@Slf4j
public class TechMessageController {

    private final TechMessageService techMessageService;

    /**
     * Get all tech messages with pagination and filtering.
     * Accessible by all authenticated users.
     *
     * @param category optional category filter
     * @param severity optional severity filter
     * @param page page number (0-indexed)
     * @param size page size
     * @param sort sort field and direction
     * @return page of tech message responses
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TechMessageResponse>> getAllTechMessages(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        log.info("Get all tech messages request - category: {}, severity: {}, page: {}, size: {}",
                category, severity, page, size);

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<TechMessageResponse> techMessages;
        if (category != null && severity != null) {
            techMessages = techMessageService.getTechMessagesByCategoryAndSeverity(
                    category, TechMessage.Severity.valueOf(severity), pageable);
        } else if (category != null) {
            techMessages = techMessageService.getTechMessagesByCategory(category, pageable);
        } else if (severity != null) {
            techMessages = techMessageService.getTechMessagesBySeverity(
                    TechMessage.Severity.valueOf(severity), pageable);
        } else {
            techMessages = techMessageService.getAllTechMessages(pageable);
        }

        return ResponseEntity.ok(techMessages);
    }

    /**
     * Get tech message by ID.
     * Accessible by all authenticated users.
     *
     * @param id tech message ID
     * @return tech message response with action levels
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TechMessageResponse> getTechMessageById(@PathVariable Long id) {
        log.info("Get tech message by ID request - id: {}", id);
        TechMessageResponse techMessage = techMessageService.getTechMessageById(id);
        return ResponseEntity.ok(techMessage);
    }

    /**
     * Get distinct categories for filtering.
     * Accessible by all authenticated users.
     *
     * @return list of unique category names
     */
    @GetMapping("/filters/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getDistinctCategories() {
        log.info("Get distinct categories request");
        List<String> categories = techMessageService.getDistinctCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Create a new tech message.
     * Accessible by ADMIN role only.
     *
     * @param request tech message creation request
     * @return created tech message response
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TechMessageResponse> createTechMessage(@Valid @RequestBody TechMessageRequest request) {
        log.info("Create tech message request - category: {}, severity: {}",
                request.getCategory(), request.getSeverity());
        TechMessageResponse techMessage = techMessageService.createTechMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(techMessage);
    }

    /**
     * Update an existing tech message.
     * Accessible by ADMIN role only.
     *
     * @param id tech message ID
     * @param request tech message update request
     * @return updated tech message response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TechMessageResponse> updateTechMessage(
            @PathVariable Long id,
            @Valid @RequestBody TechMessageRequest request) {

        log.info("Update tech message request - id: {}", id);
        TechMessageResponse techMessage = techMessageService.updateTechMessage(id, request);
        return ResponseEntity.ok(techMessage);
    }

    /**
     * Delete a tech message by ID.
     * Accessible by ADMIN role only.
     *
     * @param id tech message ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTechMessage(@PathVariable Long id) {
        log.info("Delete tech message request - id: {}", id);
        techMessageService.deleteTechMessage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add an action level to a tech message.
     * Accessible by ADMIN role only.
     *
     * @param techMessageId the tech message ID
     * @param request action level request
     * @return created action level response
     */
    @PostMapping("/{techMessageId}/actions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActionLevelResponse> addActionLevel(
            @PathVariable Long techMessageId,
            @Valid @RequestBody ActionLevelRequest request) {

        log.info("Add action level to tech message ID: {}", techMessageId);
        ActionLevelResponse actionLevel = techMessageService.addActionLevel(techMessageId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(actionLevel);
    }

    /**
     * Update an action level.
     * Accessible by ADMIN role only.
     *
     * @param actionLevelId the action level ID
     * @param request action level update request
     * @return updated action level response
     */
    @PutMapping("/actions/{actionLevelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActionLevelResponse> updateActionLevel(
            @PathVariable Long actionLevelId,
            @Valid @RequestBody ActionLevelRequest request) {

        log.info("Update action level request - id: {}", actionLevelId);
        ActionLevelResponse actionLevel = techMessageService.updateActionLevel(actionLevelId, request);
        return ResponseEntity.ok(actionLevel);
    }

    /**
     * Delete an action level.
     * Accessible by ADMIN role only.
     *
     * @param actionLevelId the action level ID
     * @return no content response
     */
    @DeleteMapping("/actions/{actionLevelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteActionLevel(@PathVariable Long actionLevelId) {
        log.info("Delete action level request - id: {}", actionLevelId);
        techMessageService.deleteActionLevel(actionLevelId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Match text against all tech message patterns.
     * Accessible by all authenticated users.
     *
     * @param errorText the text to match
     * @return pattern match response with matched tech message and recommended action
     */
    @PostMapping("/match")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PatternMatchResponse> matchText(@RequestBody String errorText) {
        log.info("Match text request");
        PatternMatchResponse matchResponse = techMessageService.matchText(errorText);
        return ResponseEntity.ok(matchResponse);
    }

    /**
     * Get message count by category.
     * Accessible by all authenticated users.
     *
     * @param category category name
     * @return message count
     */
    @GetMapping("/stats/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countMessagesByCategory(@PathVariable String category) {
        log.info("Count messages by category request - category: {}", category);
        long count = techMessageService.countMessagesByCategory(category);
        return ResponseEntity.ok(count);
    }

    /**
     * Get message count by severity.
     * Accessible by all authenticated users.
     *
     * @param severity severity level
     * @return message count
     */
    @GetMapping("/stats/severity/{severity}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countMessagesBySeverity(@PathVariable String severity) {
        log.info("Count messages by severity request - severity: {}", severity);
        long count = techMessageService.countMessagesBySeverity(TechMessage.Severity.valueOf(severity));
        return ResponseEntity.ok(count);
    }

    /**
     * Search tech messages using fuzzy keyword search and/or exact pattern matching.
     * Supports hybrid search for quick incident lookup.
     * Accessible by all authenticated users.
     *
     * @param request search request containing search text, occurrence count, and match mode
     * @return search response with matched tech messages and recommended actions
     */
    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TechMessageSearchResponse> searchTechMessages(@Valid @RequestBody TechMessageSearchRequest request) {
        log.info("Tech message search request - text: '{}', mode: {}, occurrenceCount: {}",
                request.getSearchText(), request.getMatchMode(), request.getOccurrenceCount());

        TechMessageSearchResponse response = techMessageService.searchTechMessages(request);

        log.info("Search completed - matches found: {}", response.getMatches().size());
        return ResponseEntity.ok(response);
    }
}
