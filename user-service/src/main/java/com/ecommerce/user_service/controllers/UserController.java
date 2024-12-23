package com.ecommerce.user_service.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.user_service.dto.ChangePasswordDTO;
import com.ecommerce.user_service.dto.UserRegistrationDTO;
import com.ecommerce.user_service.dto.UserUpdateDTO;
import com.ecommerce.user_service.dto.ForgotPasswordDTO;
import com.ecommerce.user_service.dto.ResetPasswordDTO;
import com.ecommerce.user_service.models.Role;
import com.ecommerce.user_service.models.User;
import com.ecommerce.user_service.repositories.RoleRepository;
import com.ecommerce.user_service.services.UserService;
import com.ecommerce.user_service.services.EmailService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearer-key")
public class UserController {
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserController(UserService userService, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // 1. Tüm kullanıcıları listeleme - Sadece ADMIN yetkisine sahip kullanıcılar
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 2. Mevcut kullanıcı bilgilerini getirme (Profile)
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(user);
    }

    // 3. Kullanıcı kaydı (Sign-Up)
    @PostMapping("/sign-up")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        // Kullanıcı var mı kontrol et
        if (userService.existsByEmail(registrationDTO.getEmail())) {
            return ResponseEntity
                .badRequest()
                .body("Email already registered");
        }

        // Yeni user oluştur
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(registrationDTO.getPassword());
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setAddress(registrationDTO.getAddress());
        user.setCreatedAt(LocalDateTime.now());

        // Varsayılan USER rolünü ata
        Role userRole = roleRepository.findByName("USER")
            .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRole(userRole);

        User savedUser = userService.createUser(user);
        return ResponseEntity.ok(savedUser);
    }

    // 4. Kullanıcı güncelleme - Sadece ADMIN veya kendi profili
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO updateDTO) {
        User user = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setFirstName(updateDTO.getFirstName());
        user.setLastName(updateDTO.getLastName());
        user.setAddress(updateDTO.getAddress());
        
        User updatedUser = userService.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    // 5. Kullanıcı silme - Sadece ADMIN yetkisine sahip kullanıcılar
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body("User deleted successfully");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangePasswordDTO changePasswordDTO) {
        
        // Mevcut kullanıcıyı bul
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Mevcut şifreyi kontrol et
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }
        
        // Yeni şifre ve onay şifresini kontrol et
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("New passwords don't match");
        }
        
        // Şifreyi güncelle
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userService.save(user);
        
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        User user = userService.findByEmail(forgotPasswordDTO.getEmail())
            .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiryDate(LocalDateTime.now().plusHours(1));
        userService.save(user);
        
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        
        return ResponseEntity.ok("Şifre sıfırlama talimatları email adresinize gönderildi");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        // Token'ı kontrol et
        User user = userService.findByResetPasswordToken(resetPasswordDTO.getToken())
            .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        // Token süresini kontrol et
        if (user.getResetPasswordTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }
        
        // Şifreleri karşılaştır
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords don't match");
        }
        
        // Şifreyi güncelle
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiryDate(null);
        userService.save(user);
        
        return ResponseEntity.ok("Password has been reset successfully");
    }
}
