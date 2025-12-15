package com.igsl.opsfinder.dto.response;

import com.igsl.opsfinder.entity.TechMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for tech message responses in API endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechMessageResponse {

    private Long id;
    private String category;
    private TechMessage.Severity severity;
    private String pattern;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<ActionLevelResponse> actionLevels = new ArrayList<>();
}
