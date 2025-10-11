package com.microservice.auth.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("otp")
public class OTP {

    @Id
    private String email;
    private String code;

    @TimeToLive
    private long expirationTime;

    public static OTP create(String email) {
        return new OTP(email, generateOtpCode(), 600L);
    }

    public boolean matches(String inputCode) {
        return this.code.equals(inputCode);
    }

    private static String generateOtpCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
