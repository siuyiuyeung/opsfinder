package com.igsl.opsfinder.dto;

import com.igsl.opsfinder.dto.response.ActionLevelResponse;
import com.igsl.opsfinder.dto.response.TechMessageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechMessageSearchResponse {
    private List<SearchMatch> matches;
    private boolean noMatches;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchMatch {
        private TechMessageResponse techMessage;
        private MatchType matchType;
        private Double matchScore;  // 0.0 to 1.0
        private String matchedText;  // For exact pattern matches
        private Map<String, String> extractedVariables;  // For exact pattern matches
        private ActionLevelResponse recommendedAction;  // Based on occurrence count
        private List<ActionLevelResponse> allActionLevels;
    }

    public enum MatchType {
        FUZZY,   // Matched by keyword search
        EXACT    // Matched by regex pattern
    }
}
