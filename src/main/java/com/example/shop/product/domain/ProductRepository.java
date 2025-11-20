package com.example.shop.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Page<Product> findAll(Pageable pageable);

    Product save(Product product);

    Optional<Product> findById(UUID id);

    void deleteById(UUID id);
}
