package com.example.shop.product.presentation;

import com.example.shop.common.ResponseEntity;
import com.example.shop.product.application.ProductService;
import com.example.shop.product.application.dto.ProductInfo;
import com.example.shop.product.presentation.dto.ProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.v1}/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "상품 목록 조회", description = "등록된 상품을 페이지 단위로 조회한다.")
    @GetMapping
    public ResponseEntity<List<ProductInfo>> findAll(Pageable pageable) {
        return productService.findAll(pageable);
    }

    @Operation(summary = "상품 등록", description = "신규 상품을 등록한다.")
    @PostMapping
    public ResponseEntity<ProductInfo> create(@RequestBody ProductRequest request) {
        return productService.create(request.toCommand());
    }

    @Operation(summary = "상품 수정", description = "상품 정보를 수정한다.")
    @PutMapping("{id}")
    public ResponseEntity<ProductInfo> update(@PathVariable("id") UUID id,
                                              @RequestBody ProductRequest request) {
        return productService.update(id, request.toCommand());
    }

    @Operation(summary = "상품 삭제", description = "상품을 삭제한다.")
    @DeleteMapping("{id}")
    public ResponseEntity<UUID> delete(@PathVariable("id") UUID id) {
        return productService.delete(id);
    }
}