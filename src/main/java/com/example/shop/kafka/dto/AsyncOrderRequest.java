package com.example.shop.kafka.dto;

import java.math.BigDecimal;
import java.util.List;

public record AsyncOrderRequest(
        String orderId,
        String memberId,
        BigDecimal totalAmount,
        List<String> itemSkus
) {
}
