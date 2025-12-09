package com.igsl.opsfinder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ErrorMessage entity representing an error pattern with regex matching.
 * Contains multiple action levels based on occurrence frequency.
 */
@Entity
@Table(name = "error_messages", indexes = {
        @Index(name = "idx_error_messages_category", columnList = "category"),
        @Index(name = "idx_error_messages_severity", columnList = "severity")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "actionLevels")
@EqualsAndHashCode(callSuper = true, exclude = "actionLevels")
public class ErrorMessage extends BaseEntity {

    /**
     * Error category (e.g., Network, Database, Application).
     */
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String category;

    /**
     * Severity level of the error.
     */
    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    /**
     * Regular expression pattern to match error messages.
     * Can include named groups for extracting variables.
     */
    @NotBlank(message = "Pattern is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String pattern;

    /**
     * Human-readable description of the error.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Action levels associated with this error message.
     * Multiple levels can be defined based on occurrence frequency.
     */
    @OneToMany(mappedBy = "errorMessage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ActionLevel> actionLevels = new ArrayList<>();

    /**
     * Severity levels for error messages.
     */
    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Add an action level to this error message.
     *
     * @param actionLevel the action level to add
     */
    public void addActionLevel(ActionLevel actionLevel) {
        actionLevels.add(actionLevel);
        actionLevel.setErrorMessage(this);
    }

    /**
     * Remove an action level from this error message.
     *
     * @param actionLevel the action level to remove
     */
    public void removeActionLevel(ActionLevel actionLevel) {
        actionLevels.remove(actionLevel);
        actionLevel.setErrorMessage(null);
    }
}
