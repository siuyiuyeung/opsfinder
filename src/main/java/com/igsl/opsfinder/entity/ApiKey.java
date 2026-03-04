package com.igsl.opsfinder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * API Key entity for external client authentication.
 * Stores hashed API keys with associated metadata and usage tracking.
 */
@Entity
@Table(name = "api_keys", indexes = {
    @Index(name = "idx_api_keys_key_hash", columnList = "key_hash", unique = true),
    @Index(name = "idx_api_keys_user_id", columnList = "user_id"),
    @Index(name = "idx_api_keys_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @Column(name = "key_prefix", nullable = false, length = 20)
    private String keyPrefix;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "rate_limit_per_hour", nullable = false)
    @Builder.Default
    private Integer rateLimitPerHour = 1000;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Long usageCount = 0L;
}
