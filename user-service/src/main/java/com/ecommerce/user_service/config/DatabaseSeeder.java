package com.ecommerce.user_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.user_service.models.Role;
import com.ecommerce.user_service.models.User;
import com.ecommerce.user_service.repositories.RoleRepository;
import com.ecommerce.user_service.repositories.UserRepository;

@Configuration
public class DatabaseSeeder {
    
    @Bean
    @SuppressWarnings("unused")
    CommandLineRunner initDatabase(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("Running CommandLineRunner: Initializing roles and admin user...");

            // Admin rolü yoksa ekle
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                Role adminRole = new Role();
                adminRole.setName("ADMIN");
                roleRepository.save(adminRole);
                System.out.println("ROLE_ADMIN created");
            }

            // User rolü yoksa ekle
            if (roleRepository.findByName("USER").isEmpty()) {
                Role userRole = new Role();
                userRole.setName("USER");
                roleRepository.save(userRole);
                System.out.println("ROLE_USER created");
            }

            // Admin kullanıcı yoksa ekle
            if (!userRepository.findByEmail("admin@example.com").isPresent()) {
                User admin = new User();
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin123")); // Şifre encode edildi
                admin.setRole(roleRepository.findByName("ADMIN").get());
                admin.setFirstName("Admin"); // Adı ekleniyor
                admin.setLastName("User");  // Soyadı ekleniyor
                admin.setAddress("Admin Address");
                userRepository.save(admin);
                System.out.println("Admin user created");
            }
        };
    }
}
