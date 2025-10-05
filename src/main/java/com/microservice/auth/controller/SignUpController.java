package com.microservice.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;
import com.microservice.auth.dto.UserDto;

import jakarta.validation.Valid;

@Controller
public class SignUpController {
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new UserDto());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@Valid @ModelAttribute("user") UserDto user,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            return "signup";
        }

        // TODO: Save user to DB
        model.addAttribute("message", "Signup successful!");
        return "signup-success";
    }
}
