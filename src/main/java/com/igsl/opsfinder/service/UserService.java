package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.request.RegisterRequest;
import com.igsl.opsfinder.dto.request.UserCreateRequest;
import com.igsl.opsfinder.dto.request.UserUpdateRequest;
import com.igsl.opsfinder.dto.response.UserResponse;
import com.igsl.opsfinder.entity.User;
import com.igsl.opsfinder.exception.BadRequestException;
import com.igsl.opsfinder.exception.ResourceNotFoundException;
import com.igsl.opsfinder.exception.UserAlreadyExistsException;
import com.igsl.opsfinder.mapper.UserMapper;
import com.igsl.opsfinder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for user management operations.
 * Handles user registration, CRUD operations, and approval workflow.
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new user.
     * User is created with active=false and role=VIEWER, requiring admin approval.
     *
     * @param request registration request with username, password, and optional full name
     * @return UserResponse with the created user information
     * @throws UserAlreadyExistsException if username already exists
     */
    public UserResponse register(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // Create user entity from request
        User user = userMapper.toEntity(request);

        // Encode password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {} (pending approval)", savedUser.getUsername());

        return userMapper.toResponse(savedUser);
    }

    /**
     * Get all users.
     *
     * @return list of all users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get users pending approval (active=false).
     *
     * @return list of users pending approval
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getPendingUsers() {
        logger.debug("Fetching pending users");
        return userRepository.findByActive(false).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID.
     *
     * @param id user ID
     * @return UserResponse with user information
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        logger.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toResponse(user);
    }

    /**
     * Create a new user (admin operation).
     * Allows admin to create users with specified role and active status.
     *
     * @param request user creation request
     * @return UserResponse with the created user information
     * @throws UserAlreadyExistsException if username already exists
     */
    public UserResponse createUser(UserCreateRequest request) {
        logger.info("Admin creating new user: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("User creation failed - username already exists: {}", request.getUsername());
            throw new UserAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        // Create user entity from request
        User user = userMapper.toEntity(request);

        // Encode password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User created successfully by admin: {}", savedUser.getUsername());

        return userMapper.toResponse(savedUser);
    }

    /**
     * Update an existing user.
     *
     * @param id user ID
     * @param request update request with fields to change
     * @return UserResponse with updated user information
     * @throws ResourceNotFoundException if user not found
     */
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        logger.info("Updating user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Update only non-null fields
        userMapper.updateEntityFromRequest(request, user);

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully: {}", updatedUser.getUsername());

        return userMapper.toResponse(updatedUser);
    }

    /**
     * Delete a user.
     *
     * @param id user ID to delete
     * @throws ResourceNotFoundException if user not found
     * @throws BadRequestException if trying to delete the last admin
     */
    public void deleteUser(Long id) {
        logger.info("Deleting user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Prevent deleting the last admin
        if (user.getRole() == User.UserRole.ADMIN) {
            long adminCount = userRepository.findByRole(User.UserRole.ADMIN).size();
            if (adminCount <= 1) {
                logger.warn("Cannot delete the last admin user: {}", user.getUsername());
                throw new BadRequestException("Cannot delete the last admin user");
            }
        }

        userRepository.delete(user);
        logger.info("User deleted successfully: {}", user.getUsername());
    }

    /**
     * Approve a pending user by setting active=true.
     *
     * @param id user ID to approve
     * @return UserResponse with approved user information
     * @throws ResourceNotFoundException if user not found
     * @throws BadRequestException if user is already approved
     */
    public UserResponse approveUser(Long id) {
        logger.info("Approving user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (Boolean.TRUE.equals(user.getActive())) {
            logger.warn("User is already approved: {}", user.getUsername());
            throw new BadRequestException("User is already approved");
        }

        user.setActive(true);
        User approvedUser = userRepository.save(user);
        logger.info("User approved successfully: {}", approvedUser.getUsername());

        return userMapper.toResponse(approvedUser);
    }

    /**
     * Reject a pending user by deleting the account.
     *
     * @param id user ID to reject
     * @throws ResourceNotFoundException if user not found
     * @throws BadRequestException if user is already approved
     */
    public void rejectUser(Long id) {
        logger.info("Rejecting user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (Boolean.TRUE.equals(user.getActive())) {
            logger.warn("Cannot reject an already approved user: {}", user.getUsername());
            throw new BadRequestException("Cannot reject an already approved user");
        }

        userRepository.delete(user);
        logger.info("User rejected and deleted: {}", user.getUsername());
    }
}
