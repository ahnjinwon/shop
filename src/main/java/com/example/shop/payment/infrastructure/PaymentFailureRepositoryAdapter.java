package com.example.shop.payment.infrastructure;

import com.example.shop.payment.domain.PaymentFailure;
import com.example.shop.payment.domain.PaymentFailureRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;


@AllArgsConstructor
@Repository
public class PaymentFailureRepositoryAdapter implements PaymentFailureRepository {

    private final PaymentFailureJpaRepository paymentFailureJpaRepository;
    @Override
    public PaymentFailure save(PaymentFailure failure) {
        return paymentFailureJpaRepository.save(failure);
    }
}
