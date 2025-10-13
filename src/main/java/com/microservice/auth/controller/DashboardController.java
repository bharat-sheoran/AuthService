package com.microservice.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(required = false) Boolean welcome, Model model) {
        
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            model.addAttribute("username", username);
            
            // Show welcome message for new users
            if (welcome != null && welcome) {
                model.addAttribute("welcomeMessage", "Welcome! Your account has been created successfully and you are now logged in.");
            }
            
            return "dashboard";
        } else {
            // If not authenticated, redirect to login
            return "redirect:/login";
        }
    }

    @GetMapping("/")
    public String home() {
        // Redirect authenticated users to dashboard, others to login
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return "redirect:/dashboard";
        } else {
            return "redirect:/login";
        }
    }
}