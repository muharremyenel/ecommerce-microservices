package com.ecommerce.user_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.user_service.services.PasswordResetService;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        passwordResetService.resetPassword(email, newPassword);
        return ResponseEntity.ok().body("Password reset successfully");
    }
} 