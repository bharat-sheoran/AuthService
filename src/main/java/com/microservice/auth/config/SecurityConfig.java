package com.microservice.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.microservice.auth.components.TokenAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        /**
         * DAO Authentication Provider for database user authentication
         */
        @Bean
        @SuppressWarnings("deprecation")
        public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userService,
                        PasswordEncoder passwordEncoder) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
                provider.setUserDetailsService(userService);
                provider.setPasswordEncoder(passwordEncoder);
                return provider;
        }

        /**
         * Security Filter Chain with JWT Token Authentication (NO SESSIONS)
         * This configuration ensures stateless authentication using JWT tokens
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userService,
                        TokenAuthFilter tokenAuthFilter) throws Exception {
                return http
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/signup/**", "/api/auth/**", "/login", "/token-login", 
                                               "/", "/error", "/css/**", "/js/**", "/images/**")
                                .permitAll()
                                .requestMatchers("/dashboard", "/api/**")
                                .authenticated()
                                .anyRequest().permitAll())
                        
                        // 🚫 DISABLE SESSION MANAGEMENT - Use stateless JWT tokens
                        .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        
                        // 🚫 DISABLE FORM LOGIN - Use JWT token authentication
                        .formLogin(form -> form.disable())
                        
                        // 🚫 DISABLE HTTP BASIC - Use JWT tokens only
                        .httpBasic(basic -> basic.disable())
                        
                        // ✅ ADD JWT TOKEN FILTER - This authenticates requests using JWT tokens
                        .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
                        
                        // Set UserDetailsService for authentication
                        .userDetailsService(userService)
                        
                        // Disable CSRF for stateless API
                        .csrf(csrf -> csrf.disable())
                        .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        }

}
