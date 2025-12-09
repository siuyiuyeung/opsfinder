package com.igsl.opsfinder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for action level responses in API endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionLevelResponse {

    private Long id;
    private Integer occurrenceMin;
    private Integer occurrenceMax;
    private String actionText;
    private Integer priority;
    private LocalDateTime createdAt;
}
