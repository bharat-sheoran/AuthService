package com.microservice.auth.services;

import com.microservice.auth.entities.User;
import com.microservice.auth.factories.UserFactory;
import com.microservice.auth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

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

        return savedUser;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
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
