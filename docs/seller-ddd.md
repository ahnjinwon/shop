# Seller Module DDD Guide

## Domain Layer

- `seller.domain.Seller`: 판매자 엔티티. 등록/수정 시 감사 필드를 관리한다.
- `seller.domain.SellerRepository`: 도메인 관점의 저장소 포트.

## Application Layer

- `seller.application.SellerService`: 판매자 조회/등록/수정/삭제 use case.
- DTOs
  - `SellerCommand`: 입력 데이터를 묶는 명령 DTO.
  - `SellerInfo`: 응답 DTO.

## Infrastructure Layer

- `seller.infrastructure.SellerJpaRepository`: Spring Data JPA 인터페이스.
- `seller.infrastructure.SellerRepositoryAdapter`: 도메인 포트를 구현해 JPA에 위임한다.

## Presentation Layer

- `seller.presentation.SellerController`: `/api/v1/sellers` REST API.
- `seller.presentation.dto.SellerRequest`: HTTP 요청 DTO.

## API 요약

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/v1/sellers` | 판매자 목록 페이지 조회 |
| `POST` | `/api/v1/sellers` | 판매자 등록 |
| `PUT` | `/api/v1/sellers/{id}` | 판매자 정보 수정 |
| `DELETE` | `/api/v1/sellers/{id}` | 판매자 삭제 |

## DDL

`docs/seller-ddl.sql`에 PostgreSQL용 `public.seller` 테이블 정의를 제공한다.
