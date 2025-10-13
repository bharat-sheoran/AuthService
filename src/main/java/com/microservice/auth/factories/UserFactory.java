package com.microservice.auth.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.microservice.auth.entities.User;

@Service
public class UserFactory {
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User create(String email, String username, String password,
            String firstName, String lastName, String phoneNumber) {
        return User.builder()
                .email(email.toLowerCase().trim())
                .username(username.trim())
                .password(passwordEncoder.encode(password))
                .firstName(firstName.trim())
                .lastName(lastName.trim())
                .phoneNumber(phoneNumber != null ? phoneNumber.trim() : null)
                .verified(true)
                .active(true)
                .build();
    }
}
