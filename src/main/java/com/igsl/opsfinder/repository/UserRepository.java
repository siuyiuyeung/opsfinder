package com.igsl.opsfinder.repository;

import com.igsl.opsfinder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    /**
     * Find users by active status.
     * Used to filter pending approval users (active=false) or active users.
     *
     * @param active active status to filter by
     * @return list of users matching the status
     */
    List<User> findByActive(Boolean active);

    /**
     * Find users by role.
     *
     * @param role user role to filter by
     * @return list of users with the specified role
     */
    List<User> findByRole(User.UserRole role);
}
