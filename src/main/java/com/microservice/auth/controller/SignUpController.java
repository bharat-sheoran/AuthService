package com.microservice.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.microservice.auth.dto.responses.ApiResponse;
import com.microservice.auth.dto.signupRequests.EmailDto;
import com.microservice.auth.dto.signupRequests.OtpVerificationRequestDto;
import com.microservice.auth.dto.signupRequests.RegisterRequestDto;
import com.microservice.auth.facades.SignupFacade;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/signup")
public class SignUpController {

    @Autowired
    private SignupFacade signupFacade;

    @PostMapping("")
    public ResponseEntity<ApiResponse> processSignup(@Valid @RequestBody EmailDto emailDto) {
        return ResponseEntity.ok(new ApiResponse(true, signupFacade.signup(emailDto.getEmail())));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> processOtpVerification(
            @Valid @RequestBody OtpVerificationRequestDto otpVerificationRequestDto) {
        String result = signupFacade.otpVerification(otpVerificationRequestDto.getEmail(),
                otpVerificationRequestDto.getOtp());
        return ResponseEntity.ok(new ApiResponse(true, result));
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse> resendOtp(@Valid @RequestBody EmailDto emailDto) {
        String result = signupFacade.resendOtp(emailDto.getEmail());
        return ResponseEntity.ok(new ApiResponse(true, result));
    }

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse> completeRegistration(
            @Valid @RequestBody RegisterRequestDto registerRequestDto) {
        String result = signupFacade.completeRegistration(registerRequestDto.getEmail(),
                registerRequestDto.getFirstName(),
                registerRequestDto.getLastName(), registerRequestDto.getUsername(),
                registerRequestDto.getPassword(), registerRequestDto.getConfirmPassword(),
                registerRequestDto.getPhoneNumber(), registerRequestDto.getAgreeTerms());
        return ResponseEntity.ok(new ApiResponse(true, result));
    }
}
