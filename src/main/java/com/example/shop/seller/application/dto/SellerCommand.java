package com.example.shop.seller.application.dto;

/**
 * 판매자 등록/수정 명령 DTO.
 */
public record SellerCommand(
        String companyName,
        String representativeName,
        String email,
        String phone,
        String businessNumber,
        String address,
        String status
) {
}
