package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.ApiKeyUsageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository for API key usage log queries.
 */
@Repository
public interface ApiKeyUsageLogRepository extends JpaRepository<ApiKeyUsageLog, Long> {

    Page<ApiKeyUsageLog> findByApiKeyIdAndRequestedAtBetween(
            Long apiKeyId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<ApiKeyUsageLog> findByApiKeyId(Long apiKeyId, Pageable pageable);

    @Query("SELECT COUNT(l) FROM ApiKeyUsageLog l")
    long countTotal();

    @Query("SELECT COUNT(l) FROM ApiKeyUsageLog l WHERE l.requestedAt >= :since")
    long countSince(@Param("since") LocalDateTime since);
}
