package com.igsl.opsfinder.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for action level creation and update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionLevelRequest {

    @NotNull(message = "Occurrence minimum is required")
    @Min(value = 1, message = "Occurrence minimum must be at least 1")
    private Integer occurrenceMin;

    private Integer occurrenceMax;

    @NotBlank(message = "Action text is required")
    private String actionText;

    @NotNull(message = "Priority is required")
    @Min(value = 1, message = "Priority must be at least 1")
    private Integer priority;
}
