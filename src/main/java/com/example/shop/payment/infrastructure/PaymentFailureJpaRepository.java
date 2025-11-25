package com.example.shop.payment.infrastructure;

import com.example.shop.payment.domain.PaymentFailure;
import com.sun.net.httpserver.Authenticator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentFailureJpaRepository extends JpaRepository<PaymentFailure, UUID> {
}
