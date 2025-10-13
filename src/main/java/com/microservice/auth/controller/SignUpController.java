package com.microservice.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;

import com.microservice.auth.dto.EmailDto;
import com.microservice.auth.services.KafkaProducer;
import com.microservice.auth.services.OtpService;
import com.microservice.auth.services.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignUpController {

    @Autowired
    private KafkaProducer kafkaProducer;
    @Autowired
    private OtpService otpService;
    @Autowired
    private UserService userService;

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("emailDto", new EmailDto());
        return "signup-email";
    }

    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute("emailDto") EmailDto emailDto,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "signup-email";
        }

        String email = emailDto.getEmail();
        
        // TODO: Convert this Email Checking Logic to UserService
        // TODO: Also Accept password this time so that user oAuth can be implemented easily
        if (userService.isEmailAlreadyRegistered(email)) {
            model.addAttribute("error", "An account with this email already exists. Please use a different email or try logging in.");
            return "signup-email";
        }
        
        try {
            kafkaProducer.sendOTP(email, otpService.createOtp(email));
            model.addAttribute("email", email);
            return "redirect:/signup/verify?email=" + email;
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "signup-email";
        }
    }

    @GetMapping("/signup/verify")
    public String verifySignup(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "signup-verify";
    }

    @PostMapping("/signup/verify")
    public String processOtpVerification(@RequestParam String email, // TODO: Add DTO in this request
            @RequestParam String otp,
            Model model) {
        try {
            boolean isValidOtp = otpService.verifyOtp(email, otp);

            if (isValidOtp) {
                // OTP is valid, proceed with user registration
                model.addAttribute("message", "Email verified successfully! You can now complete your registration.");
                model.addAttribute("email", email);
                // Redirect to complete profile or success page
                return "redirect:/signup/complete?email=" + email;
            } else {
                // Invalid OTP
                model.addAttribute("error", "Invalid verification code. Please try again.");
                model.addAttribute("email", email);
                return "signup-verify";
            }
        } catch (Exception e) {
            // Handle expired OTP or other errors
            model.addAttribute("error", "Verification code has expired or is invalid. Please request a new code.");
            model.addAttribute("email", email);
            return "signup-verify";
        }
    }

    @PostMapping("/signup/resend-otp")
    public String resendOtp(@RequestParam String email, Model model) { // TODO: Add DTO in this request
        try {
            kafkaProducer.sendOTP(email, otpService.resendOtp(email));
            model.addAttribute("message", "A new verification code has been sent to your email.");
            model.addAttribute("email", email);
            return "signup-verify";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "signup-verify";
        }
    }

    @GetMapping("/signup/complete")
    public String showCompleteRegistration(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "signup-complete";
    }

    @PostMapping("/signup/complete")
    public String completeRegistration(@RequestParam String email, // TODO: Add DTO in this request
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) Boolean agreeTerms,
            Model model) {
        try {
            if (agreeTerms == null || !agreeTerms) {
                model.addAttribute("error", "You must agree to the Terms of Service and Privacy Policy.");
                model.addAttribute("email", email);
                return "signup-complete";
            }

            // Create user account and automatically authenticate
            userService.createUser(email, username, password, firstName, lastName, phoneNumber);

            // User is now automatically authenticated, redirect to dashboard
            model.addAttribute("message", "Registration completed successfully! Welcome to your account.");
            model.addAttribute("username", username);
            return "redirect:/dashboard?welcome=true";

        } catch (IllegalArgumentException e) {
            // Validation errors from UserService
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "signup-complete";
        } catch (IllegalStateException e) {
            // Business logic errors (email/username already exists)
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "signup-complete";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again.");
            model.addAttribute("email", email);
            return "signup-complete";
        }
    }
}
