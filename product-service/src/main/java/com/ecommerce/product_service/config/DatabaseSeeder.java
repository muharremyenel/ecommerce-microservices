package com.ecommerce.product_service.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.product_service.models.Category;
import com.ecommerce.product_service.models.Product;
import com.ecommerce.product_service.repositories.CategoryRepository;
import com.ecommerce.product_service.repositories.ProductRepository;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(CategoryRepository categoryRepository, 
                                 ProductRepository productRepository) {
        return args -> {
            // Kategoriler
            Category electronics = categoryRepository.save(
                new Category(null, "Elektronik", "Elektronik ürünler")
            );
            
            Category clothing = categoryRepository.save(
                new Category(null, "Giyim", "Giyim ürünleri")
            );

            // Ürünler
            productRepository.save(
                new Product(null, "Laptop", "Yeni nesil laptop", 
                    new BigDecimal("15000"), 10, "laptop.jpg", electronics)
            );
            
            productRepository.save(
                new Product(null, "Telefon", "Akıllı telefon", 
                    new BigDecimal("8000"), 15, "phone.jpg", electronics)
            );
            
            productRepository.save(
                new Product(null, "T-Shirt", "Pamuklu t-shirt", 
                    new BigDecimal("200"), 50, "tshirt.jpg", clothing)
            );

            System.out.println("Örnek veriler başarıyla yüklendi!");
        };
    }
} 