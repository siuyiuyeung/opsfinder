package com.igsl.opsfinder.dto.request;

import com.igsl.opsfinder.entity.TechMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for tech message creation and update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechMessageRequest {

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @NotNull(message = "Severity is required")
    private TechMessage.Severity severity;

    @NotBlank(message = "Pattern is required")
    private String pattern;

    private String description;
}
