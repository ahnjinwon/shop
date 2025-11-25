## Async Kafka Sample

비동기 메시지 처리를 이해하기 위한 가벼운 샘플입니다. HTTP 요청을 받으면 즉시 `KafkaTemplate` 으로 주문 이벤트를 비동기 전송하고, 별도의 리스너가 해당 이벤트를 읽어 추가 연산을 수행하도록 구성했습니다. 전송 결과는 `CompletableFuture` 로 감싸 컨트롤러까지 전파되므로, 호출자는 카프카 브로커에 안전하게 적재되었는지 여부만 빠르게 확인하고 실제 업무 처리는 비동기 메시지로 위임할 수 있습니다.

### 주요 구성 요소

| 파일 | 설명 |
| --- | --- |
| `com.example.shop.kafka.config.AsyncKafkaConfig` | 프로듀서, 컨슈머, 토픽 생성 및 리스너 팩토리 정의 |
| `com.example.shop.kafka.application.AsyncOrderEventPublisher` | `KafkaTemplate` 의 콜백을 `CompletableFuture` 로 감싸 비동기 결과를 제공 |
| `com.example.shop.kafka.application.AsyncOrderEventListener` | `@KafkaListener` 로 주문 이벤트를 구독하고 별도 스레드에서 연산 수행 |
| `com.example.shop.kafka.presentation.AsyncOrderController` | `/api/kafka/orders` 엔드포인트에서 요청을 받고 이벤트 발행 |

### 실행 방법

1. **카프카 브로커 실행**  
   루트에 있는 `docker-compose.kafka.yml`을 사용하면 단일 노드를 바로 띄울 수 있습니다.

   ```bash
   docker compose -f docker-compose.kafka.yml up -d
   ```

   직접 명령을 작성하고 싶다면 아래처럼 Bitnami 이미지를 활용해도 됩니다.

   ```bash
   docker run -d --name shop-kafka -p 29092:29092 \
     -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,PLAINTEXT_HOST://:29092 \
     -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://shop-kafka:9092,PLAINTEXT_HOST://localhost:29092 \
     -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT \
     -e KAFKA_CFG_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
     bitnami/kafka:3.7
   ```

2. **애플리케이션 실행**

   ```bash
   ./gradlew bootRun
   ```

3. **샘플 이벤트 발행**

   ```bash
   curl -X POST http://localhost:8080/api/kafka/orders \
     -H "Content-Type: application/json" \
     -d '{
           "orderId": "ORD-20250001",
           "memberId": "MEM-101",
           "totalAmount": 125000,
           "itemSkus": ["SKU-1", "SKU-2", "SKU-3"]
         }'
   ```

   응답은 `202 Accepted` 와 함께 아래와 같은 본문을 반환합니다.

   ```json
   {
     "orderId": "ORD-20250001",
     "topic": "shop.async-orders.v1",
     "partition": 1,
     "offset": 12
   }
   ```

4. **컨슈머 로그 확인**  
   애플리케이션 로그에서 다음과 같이 발행/소비 로그를 확인할 수 있습니다.

   ```
   Async order ORD-20250001 dispatched to shop.async-orders.v1-1@12
   Completed async pipeline for order ORD-20250001 with 3 items
   ```

> 기본 설정은 `localhost:29092` 브로커 및 `shop.async-orders.v1` 토픽을 바라보도록 구성되어 있으니 필요한 경우 `application.yaml`의 `kafka` 섹션을 수정하세요.

### 커스터마이징 포인트

- `application.yaml` 의 `kafka.bootstrap-servers`, `kafka.topic.async-orders` 값을 실제 인프라에 맞게 수정합니다.
- `AsyncOrderEventPublisher` 에서 `kafkaTemplate.send` 를 호출한 뒤 바로 SLA 응답을 돌려주기 때문에, 동기 처리 구간을 최소화하고 싶다면 필요한 메타데이터만 응답하도록 구조를 단순화할 수 있습니다.
- `AsyncOrderEventListener` 에서는 현재 샘플을 위해 1초간 지연을 주고 있지만, 실제 환경에서는 결제 승인, 재고 차감 등 시간이 오래 걸리는 비즈니스 로직을 배치하면 됩니다.

샘플은 순수 자바/스프링 부트 구성만으로 작성되어 있어 원하는 모듈로 쉽게 이동할 수 있습니다. 필요 시 `docs/` 폴더의 내용을 사내 위키나 노션에 재활용하세요.
