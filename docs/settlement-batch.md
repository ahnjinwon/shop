# Seller Settlement Batch

## 개요

1. 판매자가 상품을 등록 (`/api/v1/products`, sellerId 포함)
2. 구매자가 주문을 생성 (`/api/v1/orders`)
3. Toss 결제 완료 시 `/api/v1/payments/confirm`에서
    - `purchase_order` 상태를 `PAID`로 변경
    - `seller_settlement` 테이블에 `PENDING` 상태 레코드 생성
4. Spring Batch `sellerSettlementJob`이 `PENDING` 레코드를 읽어 셀러별 합계를 계산하고 `COMPLETED`로 전환

## Batch 구성 (시각화)

```
주문/결제 흐름
 ┌──────────────┐    Toss 결제 완료    ┌──────────────────┐
 │PurchaseOrder │───────────────────▶│PaymentService    │
 └─────┬────────┘                    └──────┬───────────┘
       │       주문 상태 = PAID               │ seller_settlement 생성
       ▼                                    ▼
 ┌──────────────────┐             ┌────────────────────────────┐
 │seller_settlement │◀────────────│SellerSettlement.create(...)│
 │status = PENDING  │             └────────────────────────────┘
 └──────────────────┘

배치 실행
              ┌─────────────────────────────────────────────┐
              │           SellerSettlementJob               │
              │   (POST /api/v1/settlements/run 호출)        │
              └─────────────────────────────────────────────┘
                               │
                               ▼
         ┌─────────────────────────────┐
         │   Step: settlementStep      │
         │   (Tasklet 기반)             │
         └─────────────────────────────┘
                               │
           ┌───────────────────┴───────────────────┐
           ▼                                       ▼
 ┌─────────────────────┐                 ┌────────────────────────────┐
 │1. PENDING 조회       │                 │2. 셀러별 금액 집계             │
 │findByStatus(PENDING)│───repeat───▶    │Map<sellerId, 합계>          │
 └─────────────────────┘                 └────────────────────────────┘
           │                                        │
           ▼                                        ▼
    [레코드 목록]───markCompleted()────▶ saveAll() ──▶ 상태 COMPLETED
```

- 결과 로그: `Settled seller {sellerId} amount {합계}` 형식으로 출력해 각 셀러에게 지급될 금액을 즉시 확인할 수 있다.
- 모든 작업은 `settlementStep` Tasklet 내부에서 하나의 트랜잭션으로 처리된다.
- 실패 시 Spring Batch가 예외를 던지며 Job 상태가 FAILED로 남는다. 재실행하면 여전히 `PENDING`인 레코드만 다시 처리한다.

## 주요 용어 설명

- **seller_settlement**: 결제 승인 시 생성되는 정산 후보 레코드. 셀러별로 `PENDING` 상태로 적립된다.
- **Tasklet**: Spring Batch의 단일 작업 Step 구현체. 여기서는 조회→집계→상태 변경 과정을 한 Tasklet에서 처리한다.
- **JobParameters(timestamp, sellerId)**: 매 실행마다 고유 timestamp를 부여하고, 필요 시 `sellerId` 파라미터로 특정 셀러만 정산하도록 필터링한다.
- **Settled seller 로그**: 실제 송금 로직 대신, 현재는 어떤 셀러에게 얼마가 정산되었는지 로그로 보여주는 역할을 한다.

## 다중 작업(동시 실행) 처리

- 기본값(동기 실행): `settlement.async.enabled=false`이면 페이지 단위로 순차 실행한다. 이 경우 한 번에 하나의 쓰레드만 사용한다.
- 비동기 실행: `settlement.async.enabled=true`로 설정하면 `ThreadPoolTaskExecutor`(`settlement.async.pool-size` 기반)가 셀러별 배치를 병렬로 실행한다. `pool-size`가 늘어날수록 여러 셀러를 동시에 처리한다.
- 여전히 JobParameters에 `sellerId` 또는 timestamp를 부여하여 중복 실행을 방지한다.
- Chunk/파티셔닝 등 다른 병렬화 기법과 조합하면 대용량 정산에서도 확장 가능하다.

## 실행 방법

- `POST /api/v1/settlements/run/all`
    - 모든 판매자의 PENDING 정산을 한 번에 처리한다.
- `POST /api/v1/settlements/run/seller?sellerId={UUID}`
    - 특정 판매자의 PENDING 정산만 처리한다.
- 내부적으로 `JobLauncher`가 `sellerSettlementJob`을 timestamp JobParameters로 실행한다.
- 추후 스케줄러(Cron 등)를 붙이면 자동 정산도 가능하다.

## 00시 자동 스케줄

- `SellerSettlementScheduler`가 `@EnableScheduling`으로 동작하며 `@Scheduled(cron = "0 0 0 * * *")`으로 매일 00시에 실행된다.
- 전체 판매자를 조회한 후 각 판매자별로 `sellerId` 파라미터를 주고 배치를 순차적으로 실행한다.
- 자동 스케줄과 수동 API가 공존하므로, 필요시 수동 API로 즉시 정산을 실행하고 그렇지 않으면 자정 스케줄이 자동으로 처리한다.

## 테이블

- `seller_settlement`
    - `id`, `seller_id`, `order_id`, `amount`, `status(PENDING/COMPLETED)`, `created_at`, `settled_at`
- `purchase_order`
    - 주문과 판매자/상품/금액 정보를 보유한다 (상세는 `docs/order-ddl.sql`)

## 향후 확장 포인트

- 실제 송금 API 연동 시 Tasklet에서 외부 결제/정산 시스템 호출
- 셀러별 정산 이력 조회 API 추가
- 배치 스케줄링 (현재는 수동 API 트리거)

### 참고: Spring Batch 스키마 초기화
- Spring Boot 3.x 기준 `spring.batch.jdbc.initialize-schema` 기본값은 `embedded`여서 **임베디드 DB(H2 등)** 에서만 `schema-*.sql`이 자동 실행된다.
- PostgreSQL/MySQL 등 외부 DB에서는 `spring.batch.jdbc.initialize-schema=always`를 명시하거나, 프로젝트처럼 `ResourceDatabasePopulator`로 직접 실행해야 한다.
- 실행에 사용하는 스크립트는 클래스패스 `org/springframework/batch/core/schema-postgresql.sql`에 들어 있으며, `spring-batch-core` 의존성이 있으면 `ClassPathResource`로 바로 읽어올 수 있다. 또는 자체 SQL을 작성해 `src/main/resources`에 넣고 `ResourceDatabasePopulator`로 읽는 방법도 가능하다.
