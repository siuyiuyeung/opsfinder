package com.igsl.opsfinder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity class with common fields for all entities.
 * Provides id, createdAt, updatedAt, createdBy, and updatedBy fields
 * with automatic timestamp and user tracking management.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    /**
     * Called before persisting the entity.
     * Sets createdBy and updatedBy to the current authenticated username.
     */
    @PrePersist
    protected void onCreate() {
        String username = getCurrentUsername();
        this.createdBy = username;
        this.updatedBy = username;
    }

    /**
     * Called before updating the entity.
     * Sets updatedBy to the current authenticated username.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedBy = getCurrentUsername();
    }

    /**
     * Extract username from SecurityContext, handling anonymous users.
     *
     * @return username if authenticated, "anonymous" otherwise
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null &&
            authentication.isAuthenticated() &&
            !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return "anonymous";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity)) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
