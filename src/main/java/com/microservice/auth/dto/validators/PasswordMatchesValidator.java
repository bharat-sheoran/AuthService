package com.microservice.auth.dto.validators;

import com.microservice.auth.decorators.PasswordMatches;
import com.microservice.auth.dto.signupRequests.RegisterRequestDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequestDto> {

    @Override
    public boolean isValid(RegisterRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getPassword() == null || dto.getConfirmPassword() == null) {
            return true; // Let @NotBlank handle null/empty cases
        }
        return dto.getPassword().equals(dto.getConfirmPassword());
    }

}
