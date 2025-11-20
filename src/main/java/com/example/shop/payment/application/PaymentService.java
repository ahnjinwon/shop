package com.example.shop.payment.application;


import com.example.shop.common.ResponseEntity;
import com.example.shop.payment.application.dto.PaymentCommand;
import com.example.shop.payment.application.dto.PaymentInfo;
import com.example.shop.payment.client.TossPaymentClient;
import com.example.shop.payment.client.dto.TossPaymentResponse;
import com.example.shop.payment.domain.Payment;
import com.example.shop.payment.domain.PaymentRepository;
import com.example.shop.payment.infrastructure.PaymentJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;

    public ResponseEntity<List<PaymentInfo>> findAll(Pageable pageable) {
        Page<Payment> page = paymentRepository.findAll(pageable);
        List<PaymentInfo> paymentInfos = page.stream().map(PaymentInfo::from).toList();
        return new ResponseEntity<>(HttpStatus.OK.value(), paymentInfos, page.getTotalElements());
    }

    public ResponseEntity<PaymentInfo> confirm(PaymentCommand command) {
        TossPaymentResponse tossPayment = tossPaymentClient.confirm(command);
        Payment payment = Payment.create(
                tossPayment.paymentKey(),
                tossPayment.orderId(),
                tossPayment.totalAmount()
        );

        LocalDateTime approvedAt = tossPayment.approvedAt() != null ? tossPayment.approvedAt().toLocalDateTime() : null;
        LocalDateTime requestedAt = tossPayment.requestedAt() != null ? tossPayment.requestedAt().toLocalDateTime() : null;
        payment.markConfirmed(tossPayment.method(), approvedAt, requestedAt);

        Payment saved = paymentRepository.save(payment);
        return new ResponseEntity<>(HttpStatus.OK.value(), PaymentInfo.from(saved), 1);
    }
}
