package com.example.shop.kafka.presentation;

import com.example.shop.kafka.application.AsyncOrderEventPublisher;
import com.example.shop.kafka.dto.AsyncOrderDispatchResult;
import com.example.shop.kafka.dto.AsyncOrderRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("${api.v1}/kafka/orders")
public class AsyncOrderController {

    private final AsyncOrderEventPublisher publisher;

    public AsyncOrderController(AsyncOrderEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    // HTTP로 주문 이벤트를 받아 카프카에 위임한다.
    public CompletableFuture<ResponseEntity<AsyncOrderDispatchResult>> publish(@Valid @RequestBody AsyncOrderRequest request) {
        return publisher.publish(request)
                .thenApply(result -> ResponseEntity.accepted().body(result));
    }
}
