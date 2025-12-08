package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides database operations for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username.
     *
     * @param username username to search for
     * @return Optional containing user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if username exists.
     *
     * @param username username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);
}
