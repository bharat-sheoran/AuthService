package com.microservice.auth.components;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.microservice.auth.services.TokenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthComponent implements AuthenticationSuccessHandler {

    @Autowired
    private TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        String accessToken = tokenService.generateAccessToken(username);
        String refreshToken = tokenService.generateRefreshToken();

        ResponseCookie accessCookie = ResponseCookie.from("access_token", "Bearer " + accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict") // prevent CSRF (use 'Lax' if you have redirects)
                .maxAge(Duration.ofMinutes(15))
                .build();

        // Create Refresh Token cookie (longer lifespan)
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "Bearer " + refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh") // limit usage to refresh endpoint
                .sameSite("Strict")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        response.sendRedirect("/dashboard");
    }
}
