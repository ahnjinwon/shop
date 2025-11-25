# Product Module DDD Guide

본 문서는 제품(Product) 모듈에 적용한 DDD/헥사고날 구조를 요약한다.

## Domain Layer

- `product.domain.Product`: 상품 엔티티. 생성/수정 시 감사 필드와 기본 상태를 스스로 관리한다.
- `product.domain.ProductRepository`: 도메인 관점의 저장소 포트. 애플리케이션 서비스는 이 인터페이스에만 의존한다.

## Application Layer (Inbound Port)

- `product.application.ProductService`: 제품 use case를 노출한다. Pageable 조회, 생성, 수정, 삭제 기능을 제공하며, 응답은 `ResponseEntity`로 감싼 도메인 DTO다.
- DTOs
  - `ProductCommand`: 제품 생성/수정 입력 값을 묶는 명령 DTO.
  - `ProductInfo`: 외부로 노출되는 응답 모델.

## Presentation Layer (Inbound Adapter)

- `product.presentation.ProductController`: HTTP 요청을 받아 `ProductService`를 호출한다.
- `product.presentation.dto.ProductRequest`: HTTP 요청 페이로드를 표현하고 `toCommand()`로 `ProductCommand`를 만든다.

## Infrastructure Layer (Outbound Adapter)

- `product.infrastructure.ProductJpaRepository`: Spring Data JPA 구현체.
- `product.infrastructure.ProductRepositoryAdapter`: 도메인 `ProductRepository`를 구현해 JPA에 위임한다.

## 헥사고날/클린 적용 요약

- Controller → Request → Command → Service → Domain → Repository 순으로 단방향 의존을 유지한다.
- `ProductService`가 입력 포트, `ProductController`가 입력 어댑터 역할을 수행한다.
- `ProductRepository`는 출력 포트, `ProductRepositoryAdapter`+`ProductJpaRepository`는 출력 어댑터다.
- Request/Command/Info DTO 분리로 계층 경계가 명확하며, 도메인 엔티티는 외부 기술에 대해서 모른다.

## DDL

`docs/product-ddl.sql`에 PostgreSQL 기준 `public.product` 테이블 정의를 제공한다. 애플리케이션의 `Product` 엔티티 필드와 매핑되어 있으며, 감사 컬럼(reg/modify 정보)을 포함한다.
