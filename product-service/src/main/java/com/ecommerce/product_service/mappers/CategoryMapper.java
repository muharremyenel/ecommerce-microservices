package com.ecommerce.product_service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.product_service.dto.CategoryDTO;
import com.ecommerce.product_service.models.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "products", ignore = true)
    CategoryDTO toDTO(Category category);
    
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryDTO categoryDTO);
} 