package com.microservice.auth.services;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;

import com.microservice.auth.AppConstants;
import com.microservice.auth.entities.AccessToken;
import com.microservice.auth.entities.RefreshToken;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public TokenService() {
        // Secret key for HS256
        SecretKeySpec key = new SecretKeySpec("X9fT3pR7qJmZ2bV8yHcN4sL0aEwU6rO1".getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");

        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    public String generateAccessToken(String username) {
        return AccessToken.generate(username, jwtEncoder, AppConstants.getAccessTokenExpirationMinutes())
                .getToken();
    }

    public String generateRefreshToken(String username) {
        return RefreshToken.generate(username).getToken();
    }

    public String extractUsername(String token) {
        return AccessToken.decode(token, jwtDecoder).getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            AccessToken.decode(token, jwtDecoder);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
