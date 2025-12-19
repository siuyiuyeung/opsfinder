package com.igsl.opsfinder.controller;

import com.igsl.opsfinder.dto.request.UserCreateRequest;
import com.igsl.opsfinder.dto.request.UserUpdateRequest;
import com.igsl.opsfinder.dto.response.UserResponse;
import com.igsl.opsfinder.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for user management endpoints.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * Get all users.
     *
     * @return list of all users
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        logger.info("Admin request: Get all users");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get pending approval users.
     *
     * @return list of users with active=false
     */
    @GetMapping("/pending")
    public ResponseEntity<List<UserResponse>> getPendingUsers() {
        logger.info("Admin request: Get pending users");
        List<UserResponse> users = userService.getPendingUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID.
     *
     * @param id user ID
     * @return user information
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        logger.info("Admin request: Get user by ID: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Create a new user.
     *
     * @param request user creation request
     * @return created user information
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        logger.info("Admin request: Create new user: {}", request.getUsername());
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Update an existing user.
     *
     * @param id user ID to update
     * @param request update request with fields to change
     * @return updated user information
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        logger.info("Admin request: Update user ID: {}", id);
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Delete a user.
     *
     * @param id user ID to delete
     * @return success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        logger.info("Admin request: Delete user ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    /**
     * Approve a pending user.
     *
     * @param id user ID to approve
     * @return approved user information
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<UserResponse> approveUser(@PathVariable Long id) {
        logger.info("Admin request: Approve user ID: {}", id);
        UserResponse user = userService.approveUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Reject a pending user.
     * This will delete the user account.
     *
     * @param id user ID to reject
     * @return success message
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectUser(@PathVariable Long id) {
        logger.info("Admin request: Reject user ID: {}", id);
        userService.rejectUser(id);
        return ResponseEntity.ok(Map.of("message", "User rejected and deleted successfully"));
    }
}
