package com.microservice.auth.facades;

import com.microservice.auth.entities.RefreshToken;
import com.microservice.auth.services.KafkaProducer;
import com.microservice.auth.services.OtpService;
import com.microservice.auth.services.TokenService;
import com.microservice.auth.services.UserService;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SignupFacade {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private TokenService tokenService;

    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000L;

    public String signup(String email) {
        if (userService.isEmailAlreadyRegistered(email)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        try {
            kafkaProducer.sendOTP(email, otpService.createOtp(email));
            return "OTP has been sent to " + email;
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("Failed to send OTP.");
        }
    }

    public String otpVerification(String email, String otp) {
        boolean isValidOtp = otpService.verifyOtp(email, otp);

        if (isValidOtp) {
            return "Email verified successfully! You can now complete your registration" + email;
        } else {
            throw new IllegalArgumentException("Invalid verification code. Please try again.");
        }
    }

    public String resendOtp(String email) {
        try {
            kafkaProducer.sendOTP(email, otpService.resendOtp(email));
            return "A new verification code has been sent to your email.";
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public String completeRegistration(String email, String firstName, String lastName, String username,
            String password, String confirmPassword, String phoneNumber, Boolean agreeTerms) {

        try {
            if (agreeTerms == null || !agreeTerms) {
                return "You must agree to the Terms of Service and Privacy Policy.";
            }

            // Create user account
            userService.createUser(email, username, password, firstName, lastName, phoneNumber);

            // Generate JWT tokens for the new user
            String accessToken = tokenService.generateAccessToken(username);
            RefreshToken refreshToken = new RefreshToken(
                    tokenService.generateRefreshToken(),
                    Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_MS),
                    username);

            return "Registration successful! Access Token: " + accessToken +
                    ", Refresh Token: " + refreshToken.getToken();

        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}
