package com.ecommerce.product_service.services;

import com.ecommerce.product_service.models.Product;
import com.ecommerce.product_service.models.Category;
import com.ecommerce.product_service.repositories.ProductRepository;
import com.ecommerce.product_service.repositories.CategoryRepository;
import com.ecommerce.product_service.mappers.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.product_service.dto.ProductDTO;
import com.ecommerce.product_service.dto.ProductSearchDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.ecommerce.product_service.exceptions.ResourceNotFoundException;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, 
                        CategoryRepository categoryRepository,
                        ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProduct(Long id) {
        return productMapper.toDTO(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found with id: " + id)));
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Category not found with id: " + productDTO.getCategoryId()));
        
        Product product = productMapper.toEntity(productDTO);
        product.setCategory(category);
        
        return productMapper.toDTO(productRepository.save(product));
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Product not found with id: " + id));
        
        if (productDTO.getCategoryId() != null && 
            !productDTO.getCategoryId().equals(product.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + productDTO.getCategoryId()));
            product.setCategory(newCategory);
        }

        productMapper.updateEntityFromDTO(productDTO, product);
        return productMapper.toDTO(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public List<ProductDTO> findByFilters(ProductSearchDTO searchDTO) {
        return productRepository.findByFilters(
                searchDTO.getCategoryId(),
                searchDTO.getMinPrice(),
                searchDTO.getMaxPrice(),
                searchDTO.getInStock()
            ).stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<ProductDTO> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> findAvailableProducts() {
        return productRepository.findAvailableProducts().stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }
} 