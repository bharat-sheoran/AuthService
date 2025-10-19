package com.microservice.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;

import com.microservice.auth.services.TokenService;

@Controller
public class DashboardController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(required = false) Boolean welcome, 
                               @RequestHeader(value = "Authorization", required = false) String authHeader,
                               Model model) {
        
        // Check if user is authenticated via JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            model.addAttribute("username", username);
            model.addAttribute("authMethod", "JWT Token");
            
            // Show welcome message for new users
            if (welcome != null && welcome) {
                model.addAttribute("welcomeMessage", "Welcome! You are authenticated with JWT tokens (stateless).");
            }
            
            return "dashboard";
        } else {
            // If not authenticated with token, show login page or redirect
            model.addAttribute("error", "Please login with valid JWT token. Sessions are disabled.");
            return "token-login";
        }
    }

    @GetMapping("/")
    public String home() {
        // Check if user has valid JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return "redirect:/dashboard";
        } else {
            // Redirect to token-based login page
            return "redirect:/token-login";
        }
    }

    @GetMapping("/token-login")
    public String showTokenLogin(Model model) {
        model.addAttribute("message", "This application uses JWT token-based authentication. Sessions are disabled.");
        return "token-login";
    }
}