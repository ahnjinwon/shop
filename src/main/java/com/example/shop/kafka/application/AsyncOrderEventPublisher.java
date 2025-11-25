package com.example.shop.kafka.application;

import com.example.shop.kafka.dto.AsyncOrderDispatchResult;
import com.example.shop.kafka.dto.AsyncOrderEvent;
import com.example.shop.kafka.dto.AsyncOrderRequest;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncOrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AsyncOrderEventPublisher.class);
    private final KafkaTemplate<String, AsyncOrderEvent> kafkaTemplate;
    private final Clock clock;
    private final String topicName;

    public AsyncOrderEventPublisher(
            KafkaTemplate<String, AsyncOrderEvent> kafkaTemplate,
            Clock clock,
            @Value("${kafka.topic.async-orders:async-orders}") String topicName
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
        this.topicName = topicName;
    }

    public CompletableFuture<AsyncOrderDispatchResult> publish(AsyncOrderRequest request) {
        // HTTP 요청에서 받은 정보를 그대로 카프카 이벤트로 변환
        AsyncOrderEvent event = new AsyncOrderEvent(
                request.orderId(),
                request.memberId(),
                request.totalAmount(),
                request.itemSkus(),
                Instant.now(clock)
        );
        CompletableFuture<AsyncOrderDispatchResult> future = new CompletableFuture<>();
        // KafkaTemplate이 반환한 future를 CompletableFuture로 감싸 비동기 응답을 만든다.
        kafkaTemplate.send(topicName, event.orderId(), event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to dispatch async order event {}", event.orderId(), throwable);
                        future.completeExceptionally(throwable);
                        return;
                    }
                    if (result == null) {
                        future.completeExceptionally(new IllegalStateException("Kafka send returned null result"));
                        return;
                    }
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.info("Async order {} dispatched to {}-{}@{}", event.orderId(), metadata.topic(),
                            metadata.partition(), metadata.offset());
                    future.complete(new AsyncOrderDispatchResult(
                            event.orderId(),
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset()
                    ));
                });
        return future;
    }
}
