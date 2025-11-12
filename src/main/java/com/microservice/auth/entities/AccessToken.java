package com.microservice.auth.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.net.URL;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import lombok.Getter;

@Getter
public class AccessToken {
    private String token;
    private URL issuer;
    private Instant issuedAt;
    private Instant expiresAt;
    private String subject;
    private List<String> roles;

    private AccessToken(String token, URL issuer, Instant issuedAt, Instant expiresAt, String subject,
            List<String> roles) {
        this.token = token;
        this.issuer = issuer;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.subject = subject;
        this.roles = roles;
    }

    public static AccessToken generate(String username, JwtEncoder jwtEncoder, long expirationMinutes) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        List<String> roles = List.of("USER"); // example roles

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("your-app")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(username)
                .claim("roles", roles)
                .build();

        JwtEncoderParameters params = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims);

        String tokenValue = jwtEncoder.encode(params).getTokenValue();

        return new AccessToken(tokenValue, claims.getIssuer(), claims.getIssuedAt(),
                claims.getExpiresAt(), claims.getSubject(), roles);
    }

    public static AccessToken decode(String token, JwtDecoder jwtDecoder) {
        Jwt jwt = jwtDecoder.decode(token);

        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null)
            roles = List.of();

        return new AccessToken(token,
                jwt.getIssuer(),
                jwt.getIssuedAt(),
                jwt.getExpiresAt(),
                jwt.getSubject(),
                roles);
    }

    @Override
    public String toString() {
        return "AccessToken{token='[PROTECTED]', issuer='" + issuer + "', subject='" + subject + "', expiresAt="
                + expiresAt + ", roles=" + roles + '}';
    }
}
