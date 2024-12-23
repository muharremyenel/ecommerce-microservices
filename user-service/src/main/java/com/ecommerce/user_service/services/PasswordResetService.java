package com.ecommerce.user_service.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.user_service.models.User;

@Service
public class PasswordResetService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public void resetPassword(String email, String newPassword) {
        User user = userService.getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(user.getId(), user);
        System.out.println("Password reset successfully for user: " + email);
    }
} 