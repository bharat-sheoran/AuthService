package com.microservice.auth.services;

import com.microservice.auth.entities.User;
import com.microservice.auth.factories.UserFactory;
import com.microservice.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFactory userFactory;

    public boolean isEmailAlreadyRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

    private boolean isUsernameAlreadyTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isUserVerified(String email) {
        return userRepository.existsByEmailAndIsVerified(email);
    }

    public User createUser(String email, String username, String password,
            String firstName, String lastName, String phoneNumber) {

        if (isEmailAlreadyRegistered(email)) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        if (isUsernameAlreadyTaken(username)) {
            throw new IllegalStateException("This username is already taken. Please choose a different one.");
        }

        User user = userFactory.create(email, username, password, firstName, lastName, phoneNumber);
        User savedUser = userRepository.save(user);

        authenticateUser(savedUser);

        return savedUser;
    }

    
    private void authenticateUser(User user) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public boolean verifyUserEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setVerified(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
