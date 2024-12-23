package com.ecommerce.user_service.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.user_service.models.Role;
import com.ecommerce.user_service.models.User;
import com.ecommerce.user_service.repositories.RoleRepository;
import com.ecommerce.user_service.repositories.UserRepository;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Tüm kullanıcıları listele
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 2. Email ile kullanıcı getir
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    // 3. Kullanıcı Girişi (Login)
    public Optional<User> authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    // 4. Yeni kullanıcı oluştur (Sign-Up)
    public User createUser(User userDetails) {
        // Validasyon
        if (userDetails.getFirstName() == null || userDetails.getLastName() == null || userDetails.getEmail() == null || userDetails.getPassword() == null) {
            throw new RuntimeException("First name, last name, email, and password cannot be null");
        }

        // E-posta kontrolü (aynı e-posta ile kayıt yapılmasını engellemek için)
        if (userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        // Şifreyi şifrele
        userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));

        // Varsayılan rol ata (örneğin, ROLE_USER)
        if (userDetails.getRole() == null) {
            userDetails.setRole(defaultUserRole()); // Varsayılan bir kullanıcı rolü eklenir
        }

        // Kullanıcının oluşturulma tarihini ekle
        userDetails.setCreatedAt(LocalDateTime.now());

        // Kullanıcıyı kaydet
        return userRepository.save(userDetails);
    }

    // Varsayılan kullanıcı rolü belirle
    private Role defaultUserRole() {
        return roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
    }

    // 5. Kullanıcı bilgilerini güncelle
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setEmail(userDetails.getEmail());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setAddress(userDetails.getAddress()); // Adres alanını güncelle

        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword())); // Şifreyi güncelle
        }

        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }

        return userRepository.save(user);
    }

    // 6. Kullanıcıyı sil
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        userRepository.delete(user);
    }

    // 7. Şifre sıfırlama
    public User resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Role adından ROLE_ prefix'ini çıkar
        String roleName = user.getRole().getName().replace("ROLE_", "");
        
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(roleName)  // Spring Security otomatik olarak ROLE_ ekleyecek
                .build();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }
}
