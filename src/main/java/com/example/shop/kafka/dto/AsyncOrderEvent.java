package com.example.shop.kafka.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AsyncOrderEvent(
        String orderId,
        String memberId,
        BigDecimal totalAmount,
        List<String> itemSkus,
        Instant createdAt
) {
}
