package com.ecommerce.product_service.mappers;

import com.ecommerce.product_service.dto.ProductDTO;
import com.ecommerce.product_service.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "categoryId", source = "category.id")
    ProductDTO toDTO(Product product);

    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductDTO productDTO);

    @Mapping(target = "category", ignore = true)
    void updateEntityFromDTO(ProductDTO productDTO, @MappingTarget Product product);
} 