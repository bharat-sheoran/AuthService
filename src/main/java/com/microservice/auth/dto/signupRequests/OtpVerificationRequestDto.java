package com.microservice.auth.dto.signupRequests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpVerificationRequestDto {

    @Email(message = "Please provide a valid email")
    @NotEmpty(message = "Email is required")
    private String email;

    @Size(min = 6, max = 6, message = "OTP must be 6 characters long")
    @NotEmpty(message = "OTP is required")
    private String otp;
}
