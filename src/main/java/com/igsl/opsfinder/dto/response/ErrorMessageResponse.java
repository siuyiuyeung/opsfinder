package com.igsl.opsfinder.dto.response;

import com.igsl.opsfinder.entity.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for error message responses in API endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorMessageResponse {

    private Long id;
    private String category;
    private ErrorMessage.Severity severity;
    private String pattern;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<ActionLevelResponse> actionLevels = new ArrayList<>();
}
