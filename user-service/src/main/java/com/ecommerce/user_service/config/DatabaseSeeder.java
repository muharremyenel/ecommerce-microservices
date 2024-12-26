package com.ecommerce.user_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ecommerce.user_service.models.Role;
import com.ecommerce.user_service.repositories.RoleRepository;

@Configuration
public class DatabaseSeeder {
    
    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.count() == 0) {
                Role userRole = new Role();
                userRole.setName("ROLE_USER");
                roleRepository.save(userRole);

                Role adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                roleRepository.save(adminRole);

                System.out.println("Roller başarıyla oluşturuldu.");
            }
        };
    }
}
