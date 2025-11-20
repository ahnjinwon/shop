package com.example.shop.product.application;

import com.example.shop.common.ResponseEntity;
import com.example.shop.product.application.dto.ProductCommand;
import com.example.shop.product.application.dto.ProductInfo;
import com.example.shop.product.domain.Product;
import com.example.shop.product.domain.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public ResponseEntity<List<ProductInfo>> findAll(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        List<ProductInfo> products = page.stream()
                .map(ProductInfo::from)
                .toList();
        return new ResponseEntity<>(HttpStatus.OK.value(), products, page.getTotalElements());
    }

    public ResponseEntity<ProductInfo> create(ProductCommand command) {
        Product product = Product.create(
                command.sellerId(),
                command.name(),
                command.description(),
                command.price(),
                command.stock(),
                command.status(),
                command.operatorId()
        );
        Product saved  = productRepository.save(product);
        return new ResponseEntity<>(HttpStatus.OK.value(), ProductInfo.from(saved), 1);
    }

    public ResponseEntity<ProductInfo> update(UUID id, ProductCommand command) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Member not found"));

        UUID operator = command.operatorId() != null ? command.operatorId() : product.getModifyId();

        product.update(
                command.name(),
                command.description(),
                command.price(),
                command.stock(),
                command.status(),
                operator
        );
        Product updated = productRepository.save(product);
        return new ResponseEntity<>(HttpStatus.OK.value(), ProductInfo.from(updated), 1);
    }

    public ResponseEntity<UUID> delete(UUID id) {
        productRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK.value(), id ,1);
    }
}