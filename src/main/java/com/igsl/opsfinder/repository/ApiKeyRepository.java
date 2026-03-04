package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.ApiKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for API key CRUD operations.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    Page<ApiKey> findAll(Pageable pageable);

    @Query("SELECT COUNT(k) FROM ApiKey k WHERE k.active = true")
    long countActive();
}
