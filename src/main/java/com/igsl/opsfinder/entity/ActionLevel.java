package com.igsl.opsfinder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * ActionLevel entity representing a specific action to take based on message occurrence frequency.
 * Multiple action levels can exist for a single tech message.
 */
@Entity
@Table(name = "action_levels", indexes = {
        @Index(name = "idx_action_levels_tech_message", columnList = "tech_message_id"),
        @Index(name = "idx_action_levels_priority", columnList = "priority")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "techMessage")
@EqualsAndHashCode(callSuper = true, exclude = "techMessage")
public class ActionLevel extends BaseEntity {

    /**
     * Reference to the parent tech message.
     */
    @NotNull(message = "Tech message is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_message_id", nullable = false, foreignKey = @ForeignKey(name = "fk_action_level_tech_message"))
    private TechMessage techMessage;

    /**
     * Minimum number of occurrences for this action level to apply.
     */
    @NotNull(message = "Occurrence minimum is required")
    @Min(value = 1, message = "Occurrence minimum must be at least 1")
    @Column(name = "occurrence_min", nullable = false)
    private Integer occurrenceMin;

    /**
     * Maximum number of occurrences for this action level to apply.
     * Null means no upper limit (applies for all occurrences >= occurrenceMin).
     */
    @Column(name = "occurrence_max")
    private Integer occurrenceMax;

    /**
     * Recommended action text for this occurrence range.
     */
    @NotBlank(message = "Action text is required")
    @Column(name = "action_text", nullable = false, columnDefinition = "TEXT")
    private String actionText;

    /**
     * Priority level for this action (higher number = higher priority).
     * Used when multiple action levels match the same occurrence count.
     */
    @NotNull(message = "Priority is required")
    @Min(value = 1, message = "Priority must be at least 1")
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 1;

    /**
     * Check if this action level applies for the given occurrence count.
     *
     * @param occurrenceCount the number of times the message has occurred
     * @return true if this action level applies
     */
    public boolean appliesTo(int occurrenceCount) {
        if (occurrenceCount < occurrenceMin) {
            return false;
        }
        if (occurrenceMax == null) {
            return true; // No upper limit
        }
        return occurrenceCount <= occurrenceMax;
    }
}
