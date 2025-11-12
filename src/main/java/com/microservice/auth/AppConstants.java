package com.microservice.auth;

public class AppConstants {
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000L;
    private static final long ACCESS_TOKEN_EXPIRATION_MINUTES = 15;

    public static long getRefreshTokenExpirationMs() {
        return REFRESH_TOKEN_EXPIRATION_MS;
    }

    public static long getAccessTokenExpirationMinutes() {
        return ACCESS_TOKEN_EXPIRATION_MINUTES;
    }
}
