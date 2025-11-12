package com.microservice.auth.entities;

import java.util.Base64;
import java.util.UUID;

import com.microservice.auth.AppConstants;

import java.security.SecureRandom;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false)
    private Instant expiresAt;

    // @ManyToOne(fetch = FetchType.LAZY, optional = false)
    // @JoinColumn(name = "user_id", nullable = false)
    private String username;

    private RefreshToken(String token, Instant expiresAt, String username) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.username = username;
    }

    protected RefreshToken() {
        // JPA requirement
    }

    public static RefreshToken generate(String username) {
        return new RefreshToken(
                generateToken(),
                Instant.now().plusMillis(AppConstants.getRefreshTokenExpirationMs()),
                username);
    }

    // public RefreshToken decode(String token) {
    // // Extract username from DB using the token
    // }

    private static String generateToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    // public RefreshToken rotate(String token) {
    // // TODO: Implement token rotation logic
    // }
}
