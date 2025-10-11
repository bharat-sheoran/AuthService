package com.microservice.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {

    @Email(message = "Please provide a valid email")
    @NotEmpty(message = "Email is required")
    private String email;
}
