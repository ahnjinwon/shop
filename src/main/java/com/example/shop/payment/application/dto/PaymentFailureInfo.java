package com.example.shop.payment.application.dto;

import com.example.shop.payment.domain.PaymentFailure;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentFailureInfo(
        UUID id,
        String orderId,
        String paymentKey,
        String errorCode,
        String errorMessage,
        Long amount,
        LocalDateTime createdAt
) {

    public static PaymentFailureInfo from(PaymentFailure failure){
        return new PaymentFailureInfo(
                failure.getId(),
                failure.getOrderId(),
                failure.getPaymentKey(),
                failure.getErrorCode(),
                failure.getErrorMessage(),
                failure.getAmount(),
                failure.getCreatedAt()
        );
    }
}
