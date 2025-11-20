package com.example.shop.product.application.dto;

import com.example.shop.product.domain.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 외부에 노출되는 제품 응답 DTO.
 */
public record ProductInfo(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String status,
        LocalDateTime regDt,
        LocalDateTime modifyDt
) {

    public static ProductInfo from(Product product) {
        return new ProductInfo(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getStatus(),
                product.getRegDt(),
                product.getModifyDt()
        );
    }
}