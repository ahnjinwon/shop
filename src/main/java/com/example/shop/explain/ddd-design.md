# 백엔드 DDD 설계 가이드

### 초간단 요약 카드
- 설계: 언어 → 컨텍스트 → Aggregate → 유스케이스
- 개발: 입력 계약 → UseCase/포트 → 도메인 행위 → 어댑터 구현 → 테스트
- 계층: Interface → Application → Domain → Infrastructure
- 컨텍스트: Identity & Access(회원/Auth), Listing(매물)
- Aggregate: Listing(Root: Listing), Member(Root: Member)

이 문서는 현재 코드 베이스(`src/main/java/com/estate/webBack`, 2024-06 기준)가 도메인 주도 설계(DDD)와 헥사고날 아키텍처 원칙을 충실히 따르도록 정비·진화하기 위한 기준을 정리한 문서입니다. 실 구현은 `interfaces`, `application`, `domain`, `infrastructure` 패키지로 이미 분리되어 있으며 아래의 평가와 가이드를 바탕으로 지속 검증합니다.

## 0. 시각 요약 (한눈에 보기)
```
[Client/REST]
    ↓ DTO 변환/검증
interfaces (Controller/Mapper)
    ↓ UseCase
application (UseCase/Port)
    ↓ 도메인 협력
domain (Aggregate/VO/Service)
    ↓ Outbound Port
infrastructure (Adapter: JPA/OAuth/JWT)
    ↓
DB / External API
```

| 계층 | 역할 | 예시 패키지 |
| --- | --- | --- |
| Interface | 요청/응답 매핑, 컨트롤러 | `interfaces/**` |
| Application | 유스케이스, 트랜잭션, 권한 | `application/**` |
| Domain | Aggregate/VO, 규칙 | `domain/**` |
| Infrastructure | JPA/OAuth/JWT 어댑터 | `infrastructure/**` |

## 1. 현재 구조 점검

| 계층 | 핵심 포인트 | 코드 예시 |
| --- | --- | --- |
| Interface | Controller가 Inbound 포트만 의존, 도메인 노출 없음 | `interfaces/listing/ListingController` → `ListingQueryUseCase`/`ListingCommandUseCase` |
| Application | 유스케이스 조합, Outbound 포트만 의존 | `application/listing/ListingApplicationService` → `ListingRepositoryPort` |
| Domain | Aggregate/VO, 기술 비의존 모델 | `domain/listing/model/*`, `domain/member/model/Member` |
| Infrastructure | 포트 구현(JPA/OAuth/JWT/Hashing) | `infrastructure/listing/ListingPersistenceAdapter`, `infrastructure/auth/token/JwtTokenProvider`, `infrastructure/auth/security/SpringPasswordHasher` |
| Security | 필터도 포트에만 의존 | `interfaces/auth/security/JwtAuthenticationFilter` (토큰·멤버 포트 주입) |

### 개선 아이디어

#### 추후 수정 해야할 항목

| 우선순위 | 개선 항목 | 설명 |
| --- | --- | --- |
| 높은 | Auth 응답/이벤트 표준화 | 회원 가입/로그인 응답 DTO에 메타데이터(만료 시각, 회원 정보) 추가, 인증 이벤트 발행 및 문서화 |
| 높은 | Refresh Token 저장소 | 토큰 무효화/재발급 정책 명시, Redis/DB 포트 추가, 블랙리스트 등 안전한 재발급 처리 |
| 중간 | 도메인 이벤트 발행 | 주요 상태 전이를 이벤트로 발행, 향후 메시징 어댑터 도입해 MSA 전환 대비 |
| 중간 | 구조 검증 테스트 | 포트 단위 통합 테스트, 인터페이스 계약 테스트로 헥사 경계 회귀 차단 |

## 2. 핵심 개념 (시각 요약)

```
유비쿼터스 언어 ─→ 바운디드 컨텍스트 ─→ Aggregate ─→ 유스케이스
   (공통 용어)     (의미 경계)     (트랜잭션 묶음)    (흐름)
※ 설계 흐름 순서: 용어 합의 → 경계 설정 → 일관성 묶음 정의 → 시나리오 설계
```

| 개념 | 한 줄 정의 | EstateHub 예시 |
| --- | --- | --- |
| 유비쿼터스 언어 | 팀이 합의한 공통 용어 | 회원, 매물, 좌표, 인증 토큰 |
| 바운디드 컨텍스트 | 용어/모델의 의미 경계 | Identity & Access, Listing |
| Aggregate | Root를 가진 일관성 묶음, 트랜잭션 경계 | Listing(매물), Member(회원) |
| 유스케이스 | 목표 달성을 위한 상호작용 흐름 | 매물 등록/조회, 회원 가입/로그인 |

#### 용어 확장 설명
- **Aggregate**: 강한 일관성이 필요한 도메인 객체 묶음. Root 엔티티(ID 보유)가 불변식을 대표하고, 외부 수정은 Root를 통해서만 가능. 내부 값 객체 관리 책임도 Root에 있음.
- **Bounded Context**: 용어/모델이 일관되게 통용되는 경계. 컨텍스트 밖과는 이벤트/DTO로 번역하여 통신. 동일 용어가 다른 의미를 가질 수 있음을 명시.

### 개발 흐름 (한눈에 보기)
```
입력 계약      →    UseCase/포트      →    도메인 행위      →    어댑터 구현      →    테스트
(요청/응답 DTO)    (Inbound/Outbound)     (Aggregate 규칙)     (JPA/OAuth/JWT)      (포트·계약·슬라이스)
```

### 헥사고날(Ports & Adapters) 아키텍처 요약

![img.png](img.png)

```
[입력 어댑터] → Inbound Port(UseCase) → Application(유스케이스) → Outbound Port → [출력 어댑터]
      REST/CLI/UI                     (도메인 호출/정책)              (JPA/OAuth/JWT/외부 API)
※ 규칙: 외부 → 애플리케이션 → 도메인 단방향 의존, 도메인은 어댑터 모름
```

- 포트: 애플리케이션 계층이 정의하는 인터페이스(in/out). 도메인이 필요로 하는 외부 동작(저장소, 메시징 등) 계약.
- 어댑터: 포트를 구현하는 인프라/인터페이스 계층의 구체체. JPA, REST, OAuth, JWT 등 기술 세부사항 캡슐화.
- 장점: 포트 교체로 테스트 용이, 기술 변경 비용 감소, 경계 명확화로 결합도 최소화.

### 클린 아키텍처 관점으로 정렬하기

![img_1.png](img_1.png)
```
Entities → Use Cases → Interface Adapters → Frameworks/Drivers
 (도메인)    (유스케이스)     (컨트롤러/프리젠터/게이트웨이)   (웹/JPA/OAuth/JWT)
※ 규칙: 의존성은 안쪽으로만, 도메인·유스케이스는 프레임워크를 모른다
```

- 매핑: Entities=`domain/**`, Use Cases=`application/**`, Interface Adapters=`interfaces/**`, Frameworks/Drivers=`infrastructure/**`
- 빠른 리팩토링 포인트: 입력/출력 모델 분리, 게이트웨이(out 포트) 명확화, 프레임워크 어노테이션은 최외곽에만.
- 체크: 도메인/유스케이스가 프레임워크 타입을 모르는지? 컨트롤러가 포트만 의존하는지? 인프라 어댑터가 포트를 구현하고 직접 유스케이스를 호출하지 않는지?

### 유비쿼터스 언어 등록 표 (초안)

| 용어 | 정의/의미 | 바운디드 컨텍스트 | 코드 기준 위치 |
| --- | --- | --- | --- |
| 회원(Member) | 인증 가능한 사용자, 이메일을 정규화하고 provider/status 기본값을 보유 | Identity & Access | `src/main/java/com/estate/webBack/domain/member/model/Member.java` |
| 매물(Listing) | 부동산 매물의 기본 정보와 세부사항을 포함하는 Aggregate Root | Listing | `src/main/java/com/estate/webBack/domain/listing/model/Listing.java` |
| 좌표(Coordinate) | 위도/경도 값 객체, 비어있음 여부를 스스로 판단 | Listing | `src/main/java/com/estate/webBack/domain/listing/model/Coordinate.java` |
| 매물 유형(ListingType) | 매물 거래 타입(`rent`/`sell`)을 명시하는 값 객체 | Listing | `src/main/java/com/estate/webBack/domain/listing/model/ListingType.java` |
| 매물 상태(ListingStatus) | 노출/진행 상태(`available`, `pending`, `comingSoon` 등)를 표현하는 값 객체 | Listing | `src/main/java/com/estate/webBack/domain/listing/model/ListingStatus.java` |
| 인증 토큰(Token) | JWT 기반 API 인증 토큰을 발급/검증하는 기술 용어 | Identity & Access | `src/main/java/com/estate/webBack/infrastructure/auth/token/JwtTokenProvider.java` |

### 헥사고날 플로우 예시 (입력 어댑터 → Inbound 포트 → 유스케이스 → Outbound 포트 → 출력 어댑터)
| 흐름 | 입력 어댑터(Controller) | Inbound 포트 | 유스케이스 구현 | Outbound 포트 | 출력 어댑터 |
| --- | --- | --- | --- | --- | --- |
| Listing 조회/조작 | `interfaces/listing/ListingController` | `application/listing/port/in/ListingQueryUseCase`<br>`ListingCommandUseCase` | `application/listing/ListingApplicationService` (POJO)<br>`infrastructure/listing/ListingUseCaseFacade` (스프링/트랜잭션) | `application/listing/port/out/ListingRepositoryPort` | `infrastructure/listing/persistence/...` (JPA Adapter) |
| Auth/회원 | `interfaces/auth/AuthController` | `application/auth/port/in/AuthUseCase` | `application/auth/AuthApplicationService` (POJO)<br>`infrastructure/auth/AuthUseCaseFacade` (스프링/트랜잭션) | `application/auth/port/out/MemberRepositoryPort`<br>`TokenProviderPort`<br>`GoogleOAuthPort`/`MetaOAuthPort`<br>`PasswordHasherPort` | `infrastructure/member/persistence/...` (JPA)<br>`infrastructure/auth/token/JwtTokenProvider`<br>`infrastructure/auth/oauth/...`<br>`infrastructure/auth/security/SpringPasswordHasher` |

### 유스케이스 vs 오케스트레이션

- **유스케이스(Use Case)**: 사용자가 특정 목표를 달성하기 위해 시스템과 상호작용하는 단위 시나리오입니다. `ListingApplicationService`의 `registerListing`, `findListing` 같은 메서드는 각각 입력(명령/질의), 출력(DTO) 그리고 성공·실패 흐름을 명시하며, 도메인 모델이 어떤 순서로 호출되어야 하는지 “사용 패턴”을 정의합니다.
- **오케스트레이션(Orchestration)**: 개별 유스케이스를 실행할 때 필요한 도메인 객체, 외부 시스템, 트랜잭션을 어떤 순서로 조합·관리할지를 결정하는 행위입니다. 애플리케이션 서비스는 트랜잭션 demarcation, 권한 검사, 이벤트 발행 등 교차 관심사를 통제하면서 도메인 메서드 호출 순서를 조율하여 전체 흐름을 안정적으로 운영합니다.
- **정리**: 유스케이스는 “무엇을 위해 어떤 흐름으로 수행할지”를 설명하고, 오케스트레이션은 그 흐름을 “어떻게 기술 요소들을 연결해 실행할지”를 다룹니다. 따라서 하나의 유스케이스는 오케스트레이션 전략에 따라 동기/비동기, 직렬/병렬 등 다양한 방식으로 구현될 수 있습니다.

## 3. 제안 컨텍스트 맵

| 컨텍스트 | 책임 | 대표 패키지/클래스 |
|----------|------|--------------------|
| **Identity & Access** | 회원 가입/인증, JWT 발급 | `interfaces/auth`, `application/auth`, `domain/member`, `infrastructure/auth|member` |
| **Listing Management** | 매물 등록, 좌표/상태 관리, 추천 로직 | `interfaces/listing`, `application/listing`, `domain/listing`, `infrastructure/listing` |
| **Shared Kernel** | 공통 유틸, 설정, 에러 핸들링 | `common`, `config`, `aop`, `docs` |

컨텍스트 간 의존은 `Shared Kernel → Identity & Access → Listing` 순서를 유지합니다. Listing 컨텍스트가 회원 정보를 필요로 한다면 DTO나 도메인 이벤트를 통해 간접적으로 받아야 하며, JPA 엔티티를 직접 공유하지 않습니다.

## 4. 계층 구조

```
com.estate.webBack
├── interfaces
│   ├── auth (REST Controller, Security Filter, DTO 요청/응답)
│   └── listing (REST Controller, DTO 요청/응답/Mapper)
├── application
│   ├── auth
│   │   ├── AuthApplicationService (유스케이스)
│   │   └── port
│   │       ├── in (AuthUseCase)
│   │       └── out (MemberRepositoryPort, TokenProviderPort, OAuthPort, PasswordHasherPort)
│   └── listing
│       ├── ListingApplicationService (유스케이스)
│       ├── dto (Query, Command)
│       └── port
│           ├── in (UseCase 인터페이스)
│           └── out (ListingRepositoryPort)
├── domain
│   ├── member
│   │   └── model (Member Aggregate)
│   └── listing
│       └── model (Aggregate, Value Object)
└── infrastructure
    ├── auth
    │   ├── oauth/token/security (외부 OAuth 클라이언트, JWT Provider, PasswordHasher)
    │   └── member/persistence (JPA Entity, Mapper, Adapter)
    └── listing
        └── persistence (JpaRepository, Adapter, Mapper, Converter)
```

- **interfaces**: HTTP, CLI, 배치 등 입출력 어댑터. Swagger 문서화, 파라미터 변환, Response DTO 변환을 담당합니다.
- **application**: 유스케이스 조합, 트랜잭션 경계, 권한 검사. Inbound/Outbound 포트를 통해 도메인과 인프라를 느슨하게 연결합니다.
- **domain**: 순수 비즈니스 규칙과 불변식. Spring/JPA 의존성을 제거하고, Aggregate 메서드로 상태 변화를 표현합니다.
- **infrastructure**: DB, 메시지 브로커, 외부 API 구현체. Application 계층에서 선언한 outbound 포트를 구현합니다.

### ASCII 흐름도 (Request → Response)
```
[Client/REST]
    ↓ DTO 변환/검증
interfaces (Controller/Mapper)
    ↓ UseCase
application (UseCase/Port)
    ↓ 도메인 협력
domain (Aggregate/VO/Service)
    ↓ Outbound Port
infrastructure (Adapter: JPA/OAuth/JWT)
    ↓
DB / External API
```

## 5. Aggregate 설계 예시

### Listing Aggregate

- **Root**: `Listing` (`domain/listing/model/Listing.java`). 상태 전환(`ListingStatus`), 좌표 변경(`Coordinate`), 추천/필터링과 관련된 도메인 메서드를 내부에 두어 불변식을 보장합니다.
- **값 객체**: `Coordinate`, `ListingType`, `ListingStatus`, `ListingFilter`, `ListingDetails` 등 원시 타입을 감싸 의미와 유효성 검사를 캡슐화합니다.
- **도메인 서비스**: 여러 엔티티가 관여하거나 외부 규칙이 필요한 경우(예: 추천 계산 로직, 노출 가능 여부 판단)를 전용 도메인 서비스로 추출합니다.
- **리포지토리 포트**: `ListingRepositoryPort`는 애플리케이션 계층에서 선언되고, 인프라 계층의 `ListingPersistenceAdapter`가 이를 구현해 Spring Data JPA와 통신합니다.

### Member Aggregate

- **Root**: `Member` (`domain/member/model/Member.java`). 이메일 정규화, 공급자(provider)/상태(status) 기본값, 소셜 계정 연결 정책 등을 응집합니다.
- **행위**: `withLastLoginAt`, `withProvider`, `hasPassword`, `hasProviderUserId` 등 상태 전이를 명시적 메서드로 제공해 애플리케이션 서비스가 값 조작 대신 행위를 호출하도록 합니다.
- **확장 아이디어**: 암호, 이메일, 권한 등을 값 객체로 추출하고, 가입/비활성화 시점에 `MemberRegistered`, `MemberDeactivated` 이벤트를 발행해 다른 컨텍스트(알림, CRM 등)와 느슨하게 통합합니다.

## 6. 도메인 이벤트 & 통합

- 이벤트 클래스는 `domain.<context>.event`에 정의하고, 애플리케이션 서비스에서 `ApplicationEventPublisher`나 메시징 어댑터를 통해 발행합니다.
- 예시: 새로운 매물이 등록되면 `ListingRegistered` 이벤트를 발행 → 알림/검색 인덱싱 컨텍스트가 구독해 후속 처리를 수행합니다.
- 외부 시스템(Webhook 등) 연동은 `infrastructure` 계층의 어댑터에서 구현하고, 애플리케이션 서비스는 인터페이스에만 의존합니다.

## 7. 진화 로드맵

1. **Auth 응답/이벤트 정교화**: 회원 가입/로그인 응답 DTO에 추가 메타데이터(만료 시각, 회원 정보 등)를 제공하고, 중요 이벤트를 발행해 후속 처리를 연결합니다.
2. **Refresh Token 저장소**: 토큰 무효화 정책을 명시하고 Redis·DB 등 외부 저장소 포트를 추가해 재발급 프로세스를 견고히 합니다.
3. **값 객체 확대**: 원시 타입(`price`, `email`, `status`)을 의미 있는 값 객체로 승격해 검증 로직을 도메인에 집중시킵니다.
4. **이벤트 기반 통합**: 컨텍스트 간 직접 호출을 줄이고 도메인 이벤트/비동기 메시지를 활용해 결합도를 낮춥니다.
5. **계약/포트 테스트**: 헥사고날 경계를 검증하는 포트 단위 통합 테스트와 HTTP Contract 테스트를 추가해 구조 회귀를 방지합니다.
6. **문서/다이어그램 최신화**: 바운디드 컨텍스트, 이벤트 흐름, 의존 관계를 시각화한 다이어그램을 `docs`에 유지합니다.

## 8. 테스트 전략

- **Domain 단위 테스트**: 순수 자바 객체만으로 Aggregate, 값 객체, 도메인 서비스의 불변식과 규칙을 검증합니다.
- **Application 통합 테스트**: Mock(또는 Fake) 리포지토리를 주입해 유스케이스 흐름과 트랜잭션 경계를 검증합니다.
- **Interface/Infra 테스트**: `@WebMvcTest`, `@DataJpaTest` 등 Spring 지원을 활용하되 계층 간 경계를 침범하지 않도록 주의합니다.

## 9. 체크리스트

- [ ] 컨텍스트별 유비쿼터스 언어 목록(용어·정의·사용 컨텍스트)을 별도 문서/위키에 정리하고, 분기마다 실제 코드·API에서 유지되는지 리뷰했는가?
- [ ] Domain 패키지에서 Spring/JPA 의존성을 완전히 제거했는가?
- [ ] 애플리케이션 서비스가 도메인 메서드/포트만 호출하고 있는가?
- [ ] Infrastructure 구현체가 모두 인터페이스(포트) 뒤에 숨겨져 있는가?
- [ ] 도메인 이벤트 흐름을 다이어그램과 문서로 공유했는가?

이 가이드에 맞춰 구조를 점검하고 개선하면 EstateHub 백엔드는 DDD 원칙에 부합하는 유연성과 확장성을 유지할 수 있습니다.
