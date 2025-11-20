package com.example.shop.seller.application.dto;

import com.example.shop.seller.domain.Seller;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 판매자 응답 DTO.
 */
public record SellerInfo(
        UUID id,
        String companyName,
        String representativeName,
        String email,
        String phone,
        String businessNumber,
        String address,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SellerInfo from(Seller seller) {
        return new SellerInfo(
                seller.getId(),
                seller.getCompanyName(),
                seller.getRepresentativeName(),
                seller.getEmail(),
                seller.getPhone(),
                seller.getBusinessNumber(),
                seller.getAddress(),
                seller.getStatus(),
                seller.getCreatedAt(),
                seller.getUpdatedAt()
        );
    }
}