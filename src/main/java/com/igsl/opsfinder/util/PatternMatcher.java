package com.igsl.opsfinder.util;

import com.igsl.opsfinder.entity.TechMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility class for matching technical messages against regex patterns.
 * Extracts named groups from patterns as variables.
 */
@Component
@Slf4j
public class PatternMatcher {

    /**
     * Match text against a list of tech message patterns.
     *
     * @param errorText the text to match
     * @param techMessages list of tech messages with regex patterns
     * @return optional match result containing the matched tech message and extracted variables
     */
    public Optional<MatchResult> matchMessage(String errorText, List<TechMessage> techMessages) {
        if (errorText == null || errorText.isBlank()) {
            log.debug("Text is null or blank, no match possible");
            return Optional.empty();
        }

        for (TechMessage techMessage : techMessages) {
            try {
                Pattern pattern = Pattern.compile(techMessage.getPattern(), Pattern.DOTALL);
                Matcher matcher = pattern.matcher(errorText);

                if (matcher.find()) {
                    log.debug("Matched tech message ID {} with pattern: {}",
                            techMessage.getId(), techMessage.getPattern());

                    // Extract named groups as variables
                    Map<String, String> variables = extractNamedGroups(matcher);

                    return Optional.of(MatchResult.builder()
                            .techMessage(techMessage)
                            .variables(variables)
                            .matchedText(matcher.group(0))
                            .build());
                }
            } catch (PatternSyntaxException e) {
                log.error("Invalid regex pattern for tech message ID {}: {}",
                        techMessage.getId(), e.getMessage());
                // Continue trying other patterns
            } catch (Exception e) {
                log.error("Error matching pattern for tech message ID {}: {}",
                        techMessage.getId(), e.getMessage());
            }
        }

        log.debug("No pattern matched for text: {}", errorText);
        return Optional.empty();
    }

    /**
     * Extract named groups from a matcher as a map of variables.
     *
     * @param matcher the regex matcher with named groups
     * @return map of variable names to values
     */
    private Map<String, String> extractNamedGroups(Matcher matcher) {
        Map<String, String> variables = new HashMap<>();

        try {
            // Get all named groups from the pattern
            Pattern pattern = matcher.pattern();
            String patternString = pattern.pattern();

            // Extract named group names using regex
            Pattern namedGroupPattern = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");
            Matcher namedGroupMatcher = namedGroupPattern.matcher(patternString);

            while (namedGroupMatcher.find()) {
                String groupName = namedGroupMatcher.group(1);
                try {
                    String groupValue = matcher.group(groupName);
                    if (groupValue != null) {
                        variables.put(groupName, groupValue);
                        log.debug("Extracted variable: {} = {}", groupName, groupValue);
                    }
                } catch (IllegalArgumentException e) {
                    log.debug("Named group '{}' not found in match", groupName);
                }
            }
        } catch (Exception e) {
            log.error("Error extracting named groups: {}", e.getMessage());
        }

        return variables;
    }

    /**
     * Validate a regex pattern for syntax errors.
     *
     * @param patternString the regex pattern to validate
     * @return validation result with error message if invalid
     */
    public ValidationResult validatePattern(String patternString) {
        try {
            Pattern.compile(patternString);
            return ValidationResult.builder()
                    .valid(true)
                    .build();
        } catch (PatternSyntaxException e) {
            return ValidationResult.builder()
                    .valid(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Result of pattern matching.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MatchResult {
        private TechMessage techMessage;
        private Map<String, String> variables;
        private String matchedText;
    }

    /**
     * Result of pattern validation.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationResult {
        private boolean valid;
        private String errorMessage;
    }
}
