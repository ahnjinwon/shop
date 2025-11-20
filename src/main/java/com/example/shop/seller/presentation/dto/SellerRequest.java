package com.example.shop.seller.presentation.dto;

import com.example.shop.seller.application.dto.SellerCommand;

public record SellerRequest(
        String companyName,
        String representativeName,
        String email,
        String phone,
        String businessNumber,
        String address,
        String status
) {

    public SellerCommand toCommand() {
        return new SellerCommand(companyName, representativeName, email, phone, businessNumber, address, status);
    }
}
