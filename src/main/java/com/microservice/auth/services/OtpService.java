package com.microservice.auth.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microservice.auth.entities.OTP;
import com.microservice.auth.repositories.OtpRepository;

@Service
public class OtpService {
    @Autowired
    private OtpRepository otpRepository;

    public OTP createOtp(String email) {
        Optional<OTP> existingOtp = otpRepository.findById(email);
        if (existingOtp.isPresent()) {
            throw new IllegalStateException("Verification OTP already sent. Please check your email or wait before requesting a new one.");
        }
        OTP otp = OTP.create(email);
        return otpRepository.save(otp);
    }

    public OTP resendOtp(String email) {
        OTP otp = OTP.create(email);
        return otpRepository.save(otp);
    }

    public boolean verifyOtp(String email, String code) {
        return otpRepository.findById(email)
                .filter(stored -> stored.matches(code))
                .map(stored -> {
                    otpRepository.deleteById(email); // invalidate after verification
                    return true;
                })
                .orElse(false);
    }
}
