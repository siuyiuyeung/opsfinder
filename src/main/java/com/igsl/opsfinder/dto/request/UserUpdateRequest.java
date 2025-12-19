package com.igsl.opsfinder.dto.request;

import com.igsl.opsfinder.entity.User;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating existing users.
 * All fields are optional to allow partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    private User.UserRole role;

    private Boolean active;
}
