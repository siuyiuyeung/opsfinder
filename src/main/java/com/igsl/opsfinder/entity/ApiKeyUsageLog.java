package com.igsl.opsfinder.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Append-only audit log for API key usage.
 * Does not extend BaseEntity — no update semantics needed, and async context has no SecurityContext.
 */
@Entity
@Table(name = "api_key_usage_logs", indexes = {
    @Index(name = "idx_api_key_usage_logs_api_key_id", columnList = "api_key_id"),
    @Index(name = "idx_api_key_usage_logs_requested_at", columnList = "requested_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_key_id", nullable = false)
    private Long apiKeyId;

    @Column(nullable = false, length = 255)
    private String endpoint;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "client_ip", nullable = false, length = 45)
    private String clientIp;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;
}
