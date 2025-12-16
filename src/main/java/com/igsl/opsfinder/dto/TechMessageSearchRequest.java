package com.igsl.opsfinder.dto;

import lombok.Data;

@Data
public class TechMessageSearchRequest {
    private String searchText;
    private Integer occurrenceCount;
    private MatchMode matchMode = MatchMode.BOTH;

    public enum MatchMode {
        FUZZY,   // Keyword-based search
        EXACT,   // Regex pattern matching
        BOTH     // Hybrid: fuzzy + exact
    }
}
