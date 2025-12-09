package com.igsl.opsfinder.controller;

import com.igsl.opsfinder.dto.request.ActionLevelRequest;
import com.igsl.opsfinder.dto.request.ErrorMessageRequest;
import com.igsl.opsfinder.dto.response.ActionLevelResponse;
import com.igsl.opsfinder.dto.response.ErrorMessageResponse;
import com.igsl.opsfinder.dto.response.PatternMatchResponse;
import com.igsl.opsfinder.entity.ErrorMessage;
import com.igsl.opsfinder.service.ErrorMessageService;
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
 * REST controller for error message management operations.
 * Endpoints are protected with role-based access control.
 */
@RestController
@RequestMapping("/api/errors")
@RequiredArgsConstructor
@Slf4j
public class ErrorMessageController {

    private final ErrorMessageService errorMessageService;

    /**
     * Get all error messages with pagination and filtering.
     * Accessible by all authenticated users.
     *
     * @param category optional category filter
     * @param severity optional severity filter
     * @param page page number (0-indexed)
     * @param size page size
     * @param sort sort field and direction
     * @return page of error message responses
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ErrorMessageResponse>> getAllErrorMessages(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        log.info("Get all error messages request - category: {}, severity: {}, page: {}, size: {}",
                category, severity, page, size);

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<ErrorMessageResponse> errorMessages;
        if (category != null && severity != null) {
            errorMessages = errorMessageService.getErrorMessagesByCategoryAndSeverity(
                    category, ErrorMessage.Severity.valueOf(severity), pageable);
        } else if (category != null) {
            errorMessages = errorMessageService.getErrorMessagesByCategory(category, pageable);
        } else if (severity != null) {
            errorMessages = errorMessageService.getErrorMessagesBySeverity(
                    ErrorMessage.Severity.valueOf(severity), pageable);
        } else {
            errorMessages = errorMessageService.getAllErrorMessages(pageable);
        }

        return ResponseEntity.ok(errorMessages);
    }

    /**
     * Get error message by ID.
     * Accessible by all authenticated users.
     *
     * @param id error message ID
     * @return error message response with action levels
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ErrorMessageResponse> getErrorMessageById(@PathVariable Long id) {
        log.info("Get error message by ID request - id: {}", id);
        ErrorMessageResponse errorMessage = errorMessageService.getErrorMessageById(id);
        return ResponseEntity.ok(errorMessage);
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
        List<String> categories = errorMessageService.getDistinctCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Create a new error message.
     * Accessible by ADMIN role only.
     *
     * @param request error message creation request
     * @return created error message response
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ErrorMessageResponse> createErrorMessage(@Valid @RequestBody ErrorMessageRequest request) {
        log.info("Create error message request - category: {}, severity: {}",
                request.getCategory(), request.getSeverity());
        ErrorMessageResponse errorMessage = errorMessageService.createErrorMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(errorMessage);
    }

    /**
     * Update an existing error message.
     * Accessible by ADMIN role only.
     *
     * @param id error message ID
     * @param request error message update request
     * @return updated error message response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ErrorMessageResponse> updateErrorMessage(
            @PathVariable Long id,
            @Valid @RequestBody ErrorMessageRequest request) {

        log.info("Update error message request - id: {}", id);
        ErrorMessageResponse errorMessage = errorMessageService.updateErrorMessage(id, request);
        return ResponseEntity.ok(errorMessage);
    }

    /**
     * Delete an error message by ID.
     * Accessible by ADMIN role only.
     *
     * @param id error message ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteErrorMessage(@PathVariable Long id) {
        log.info("Delete error message request - id: {}", id);
        errorMessageService.deleteErrorMessage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add an action level to an error message.
     * Accessible by ADMIN role only.
     *
     * @param errorMessageId the error message ID
     * @param request action level request
     * @return created action level response
     */
    @PostMapping("/{errorMessageId}/actions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActionLevelResponse> addActionLevel(
            @PathVariable Long errorMessageId,
            @Valid @RequestBody ActionLevelRequest request) {

        log.info("Add action level to error message ID: {}", errorMessageId);
        ActionLevelResponse actionLevel = errorMessageService.addActionLevel(errorMessageId, request);
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
        ActionLevelResponse actionLevel = errorMessageService.updateActionLevel(actionLevelId, request);
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
        errorMessageService.deleteActionLevel(actionLevelId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Match error text against all error message patterns.
     * Accessible by all authenticated users.
     *
     * @param errorText the error text to match
     * @return pattern match response with matched error message and recommended action
     */
    @PostMapping("/match")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PatternMatchResponse> matchErrorText(@RequestBody String errorText) {
        log.info("Match error text request");
        PatternMatchResponse matchResponse = errorMessageService.matchErrorText(errorText);
        return ResponseEntity.ok(matchResponse);
    }

    /**
     * Get error count by category.
     * Accessible by all authenticated users.
     *
     * @param category category name
     * @return error count
     */
    @GetMapping("/stats/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countErrorsByCategory(@PathVariable String category) {
        log.info("Count errors by category request - category: {}", category);
        long count = errorMessageService.countErrorsByCategory(category);
        return ResponseEntity.ok(count);
    }

    /**
     * Get error count by severity.
     * Accessible by all authenticated users.
     *
     * @param severity severity level
     * @return error count
     */
    @GetMapping("/stats/severity/{severity}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> countErrorsBySeverity(@PathVariable String severity) {
        log.info("Count errors by severity request - severity: {}", severity);
        long count = errorMessageService.countErrorsBySeverity(ErrorMessage.Severity.valueOf(severity));
        return ResponseEntity.ok(count);
    }
}
