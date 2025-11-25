# Order Module

## Domain
- `order.domain.PurchaseOrder`: 구매 주문 엔티티 (상품, 판매자, 구매자, 금액, 상태).
- `order.domain.PurchaseOrderRepository`: 도메인 포트.

## Application
- `order.application.OrderService`
  - `create(OrderCommand)`: 상품 가격과 판매자 정보를 기반으로 주문 생성.
  - `findAll(Pageable)`: 주문 목록 조회.
  - `findEntity`, `markPaid`: 결제 모듈에서 주문 상태 변경 시 사용.
- DTOs: `OrderCommand`, `OrderInfo`.

## Infrastructure
- `order.infrastructure.PurchaseOrderJpaRepository`
- `order.infrastructure.PurchaseOrderRepositoryAdapter`

## Presentation
- `order.presentation.OrderController`
  - `POST /api/v1/orders`: 주문 생성(결제 전 Toss orderId로 사용).
  - `GET /api/v1/orders`: 주문 목록 페이징 조회.

## DDL
- `docs/order-ddl.sql` 참고.
