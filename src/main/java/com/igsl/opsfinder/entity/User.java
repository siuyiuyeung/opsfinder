package com.igsl.opsfinder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * User entity for authentication and authorization.
 * Represents system users with role-based access control.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_username", columnList = "username"),
    @Index(name = "idx_users_role", columnList = "role"),
    @Index(name = "idx_users_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    @Column(name = "full_name", length = 100)
    private String fullName;

    @NotBlank(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * User role enum defining access levels in the system.
     */
    public enum UserRole {
        /**
         * Administrator - full system access
         */
        ADMIN,

        /**
         * Operator - can create and modify devices, errors, and incidents
         */
        OPERATOR,

        /**
         * Viewer - read-only access
         */
        VIEWER
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}
