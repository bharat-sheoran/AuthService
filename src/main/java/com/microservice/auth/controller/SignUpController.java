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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SignUpController {
    @Autowired
    private final OtpService otpService;

    @Autowired
    private final KafkaProducer kafkaProducer;

    SignUpController(KafkaProducer kafkaProducer, OtpService otpService) {
        this.kafkaProducer = kafkaProducer;
        this.otpService = otpService;
    }

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

        String otp = String.valueOf((int) ((Math.random() * 900000) + 100000));
        String email = emailDto.getEmail();
        otpService.saveOtp(email, otp);
        kafkaProducer.sendOtp(email, otp);
        model.addAttribute("email", email);
        return "redirect:/signup/verify?email=" + email;
    }

    @GetMapping("/signup/verify")
    public String verifySignup(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "signup-verify";
    }

    @PostMapping("/signup/verify")
    public String processOtpVerification(@RequestParam String email,
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
    public String resendOtp(@RequestParam String email, Model model) {
        try {
            // Generate new OTP
            String newOtp = String.valueOf((int) ((Math.random() * 900000) + 100000));
            otpService.saveOtp(email, newOtp);
            kafkaProducer.sendOtp(email, newOtp);
            
            model.addAttribute("message", "A new verification code has been sent to your email.");
            model.addAttribute("email", email);
            return "signup-verify";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to resend verification code. Please try again.");
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
    public String completeRegistration(@RequestParam String email,
                                     @RequestParam String firstName,
                                     @RequestParam String lastName,
                                     @RequestParam String username,
                                     @RequestParam String password,
                                     @RequestParam String confirmPassword,
                                     @RequestParam(required = false) String phoneNumber,
                                     @RequestParam(required = false) Boolean agreeTerms,
                                     Model model) {
        try {
            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Passwords do not match.");
                model.addAttribute("email", email);
                return "signup-complete";
            }

            // Validate terms agreement
            if (agreeTerms == null || !agreeTerms) {
                model.addAttribute("error", "You must agree to the Terms of Service and Privacy Policy.");
                model.addAttribute("email", email);
                return "signup-complete";
            }

            // TODO: Create user account in database
            // UserService.createUser(email, firstName, lastName, username, password, phoneNumber);
            
            model.addAttribute("message", "Registration completed successfully! Please log in with your credentials.");
            model.addAttribute("username", username);
            return "redirect:/login?registered=true";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again.");
            model.addAttribute("email", email);
            return "signup-complete";
        }
    }

}
