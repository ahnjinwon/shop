package com.example.shop.payment.presentation.dto;

import com.example.shop.payment.application.dto.PaymentFailCommand;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentFailRequest(
        String orderId,
        String paymentKey,
        String errorCode,
        String errorMessage,
        Long amount,
        String rawPayload
) {
    public PaymentFailCommand toCommand(){
    return new PaymentFailCommand(orderId, paymentKey, errorCode, errorMessage, amount, rawPayload);
    }
}
