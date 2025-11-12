package com.microservice.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import com.microservice.auth.entities.RefreshToken;
import com.microservice.auth.repositories.RefreshTokenRepository;
import com.microservice.auth.services.TokenService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for JWT token-based authentication
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * Login endpoint that returns JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                // Generate JWT tokens
                String accessToken = tokenService.generateAccessToken(authentication.getName());
                RefreshToken refreshToken = RefreshToken.generate(authentication.getName());

                refreshTokenRepository.save(refreshToken);

                Map<String, Object> tokenResponse = new HashMap<>();
                tokenResponse.put("accessToken", accessToken);
                tokenResponse.put("refreshToken", refreshToken);
                tokenResponse.put("tokenType", "Bearer");
                tokenResponse.put("expiresIn", 900); // 15 minutes in seconds
                tokenResponse.put("username", authentication.getName());

                return ResponseEntity.ok(tokenResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication failed"));
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();

            if (tokenService.isTokenValid(refreshToken)) {
                String username = tokenService.extractUsername(refreshToken);
                String newAccessToken = tokenService.generateAccessToken(username);

                Map<String, Object> tokenResponse = new HashMap<>();
                tokenResponse.put("accessToken", newAccessToken);
                tokenResponse.put("refreshToken", refreshToken); // Keep the same refresh token
                tokenResponse.put("tokenType", "Bearer");
                tokenResponse.put("expiresIn", 900); // 15 minutes in seconds
                tokenResponse.put("username", username);

                return ResponseEntity.ok(tokenResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired refresh token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * Get user information from JWT token
     */
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing or invalid authorization header"));
            }

            String token = authorizationHeader.substring(7);

            if (tokenService.isTokenValid(token)) {
                String username = tokenService.extractUsername(token);

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("username", username);
                userInfo.put("authenticated", true);
                userInfo.put("tokenValid", true);

                return ResponseEntity.ok(userInfo);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token validation failed"));
        }
    }

    // Request DTOs
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RefreshTokenRequest {
        private String refreshToken;

        // Getters and setters
        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}