package com.igsl.opsfinder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for pattern matching API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatternMatchResponse {

    private boolean matched;
    private ErrorMessageResponse errorMessage;
    private String matchedText;

    @Builder.Default
    private Map<String, String> variables = new HashMap<>();

    private String recommendedAction;
    private Integer occurrenceCount;
}
