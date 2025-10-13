package com.microservice.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/signup/**", "/register", "/login", "/css/**",
                                                                "/js/**")
                                                .permitAll()
                                                .requestMatchers("/dashboard", "/")
                                                .authenticated()
                                                // 🔒 Everything else requires authentication
                                                .anyRequest().authenticated())
                                // ✅ Enable form login for protected routes
                                .formLogin(Customizer.withDefaults())
                                .logout(Customizer.withDefaults())
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/signup/**", "/register"))
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        }

}
