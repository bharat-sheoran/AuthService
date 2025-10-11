package com.microservice.auth.services;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpService {
    private final StringRedisTemplate redisTemplate;

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveOtp(String email, String otp) {
        // Store OTP with 10-minute expiry
        redisTemplate.opsForValue().set(email, otp, 10, TimeUnit.MINUTES);
    }

    public String getOtp(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    public boolean verifyOtp(String email, String otp) {
        String storedOtp = getOtp(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            // OTP is valid, remove it from Redis to prevent reuse
            redisTemplate.delete(email);
            return true;
        }
        return false;
    }

    public boolean isOtpExpired(String email) {
        return getOtp(email) == null;
    }

    public void deleteOtp(String email) {
        redisTemplate.delete(email);
    }
}
