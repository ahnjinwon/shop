package com.example.shop.seller.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SellerRepository {
    Page<Seller> findAll(Pageable pageable);

    Seller save(Seller seller);

    Optional<Seller> findById(UUID id);

    void deleteById(UUID id);
}
