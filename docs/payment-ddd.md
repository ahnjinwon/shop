# Payment Module & Toss Integration

## DDL

`docs/payment-ddl.sql`에 `public.payment` 테이블 스키마를 정의했다. `Payment` 엔티티와 매핑되며 토스 결제 키, 주문번호, 금액, 상태, 승인/요청 일시를 저장한다.

## Hexagonal Layers

- **Domain**
  - `payment.domain.Payment`: 결제 엔티티, 상태 전이(`markConfirmed`, `markFailed`) 포함.
  - `payment.domain.PaymentRepository`: 도메인 포트.
  - `payment.domain.PaymentFailure`: 실패 로그 엔티티.
  - `payment.domain.PaymentFailureRepository`: 실패 로그 포트.
- **Infrastructure (Outbound Adapter)**
  - `payment.infrastructure.PaymentRepositoryAdapter` + `PaymentJpaRepository`: JPA 구현을 감싼 저장소 어댑터.
  - `payment.infrastructure.PaymentFailureRepositoryAdapter` + `PaymentFailureJpaRepository`: 실패 로그 저장소 어댑터.
- **Application (Inbound Port)**
  - `payment.application.PaymentService`: 결제 조회/승인/실패 기록, 주문 정산 레코드 생성.
  - DTOs: `PaymentCommand`(성공 입력), `PaymentInfo`(성공 응답), `PaymentFailCommand`/`PaymentFailureInfo`(실패 기록).
- **Presentation (Inbound Adapter)**
  - `payment.presentation.PaymentController`: `/api/v1/payments` REST API.
  - `payment.presentation.dto.PaymentRequest`, `PaymentFailRequest`: HTTP 요청 DTO.

## Toss Payments 연동

- 설정은 `payment.toss.*` 프로퍼티로 주입되며 `.env`의 `TOSS_SECRET_KEY`, `TOSS_SUCCESS_URL`, `TOSS_FAIL_URL`로 관리한다.
- `payment.client.TossPaymentClient`가 토스 결제 승인 API (`/v1/payments/confirm`)를 호출한다.
  - Basic 인증 헤더를 secret key로 구성.
  - 실패 시 토스 응답 메시지를 포함해 예외 발생.
- `payment.client.dto.TossPaymentResponse`는 토스 응답(JSON)을 Jackson으로 역직렬화한다.
- `PaymentService.confirm()`에서 토스 응답으로 결제 엔티티를 생성/저장 후 `PaymentInfo`로 반환한다.

## API 요약

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/v1/payments` | 결제 내역 페이지 조회 (page/size/ sort 파라미터 지원). |
| `POST` | `/api/v1/payments/confirm` | 프론트에서 전달한 `paymentKey`, `orderId`, `amount`로 토스 결제 승인 후 DB에 저장. |
| `POST` | `/api/v1/payments/fail` | 결제 실패 정보를 로그 테이블에 저장. |
| `POST` | `/api/v1/settlements/run` | 대기 중인 판매자 정산을 Spring Batch로 실행. |

## 환경 변수

`.env` 예시   

```
TOSS_SECRET_KEY=replace-with-secret
TOSS_SUCCESS_URL=http://localhost:8080/payments/success
TOSS_FAIL_URL=http://localhost:8080/payments/fail
```

애플리케이션 실행 전 `source .env` 또는 배포 환경 변수에 동일한 키를 셋업해야 한다.
