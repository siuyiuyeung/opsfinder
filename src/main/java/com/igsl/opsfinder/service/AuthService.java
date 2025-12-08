package com.igsl.opsfinder.service;

import com.igsl.opsfinder.dto.request.LoginRequest;
import com.igsl.opsfinder.dto.response.AuthResponse;
import com.igsl.opsfinder.dto.response.UserResponse;
import com.igsl.opsfinder.entity.User;
import com.igsl.opsfinder.repository.UserRepository;
import com.igsl.opsfinder.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service for authentication operations.
 * Handles login, token generation, and user retrieval.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Authenticate user and generate JWT tokens.
     *
     * @param loginRequest login credentials
     * @return AuthResponse with JWT tokens and user info
     */
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getUsername());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginRequest.getUsername()));

        // Generate tokens
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        logger.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Refresh access token using refresh token.
     *
     * @param refreshToken refresh token
     * @return new access token
     */
    public String refreshAccessToken(String refreshToken) {
        if (tokenProvider.validateToken(refreshToken)) {
            String username = tokenProvider.getUsernameFromToken(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            String newAccessToken = tokenProvider.generateTokenFromUsername(
                    user.getUsername(),
                    "ROLE_" + user.getRole().name()
            );

            logger.info("Access token refreshed for user: {}", username);
            return newAccessToken;
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    /**
     * Get current authenticated user information.
     *
     * @return UserResponse with current user info
     */
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
