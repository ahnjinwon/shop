# Request vs Command in the Member Module

이 문서는 현재 회원(Member) 모듈에서 `Request`와 `Command`를 분리하는 이유와 각 DTO가 맡은 역할을 정리한다. 두 타입 모두 동일한 필드를 보유하지만, DDD 관점에서 서로 다른 계층 책임을 유지하기 위해 의도적으로 나누었다.

## 계층별 책임

| DTO | 위치 | 책임 |
| --- | --- | --- |
| `MemberRequest` | presentation (`member.presentation.dto`) | HTTP/웹 요청 페이로드 표현. UI의 필드명, validation 어노테이션, Swagger 문서 등 “전달 형식”에 대한 의존성을 허용한다. |
| `MemberCommand` | application (`member.application.dto`) | Use-case 입력 모델. 컨트롤러 외 다른 진입점(배치, 메시지 큐 등)에서도 동일하게 사용할 수 있도록 HTTP와 무관한 순수 데이터만 담는다. |

`MemberRequest`는 `toCommand()` 변환 메서드를 통해 애플리케이션 계층으로 필요한 값만 넘기며, 그 이후 로직은 `MemberCommand`만 바라본다.

## 분리의 장점

1. **계층 독립성**  
   프레젠테이션 변경(필드명, 직렬화 옵션, 인증 토큰 등)이 애플리케이션/도메인 계층을 오염시키지 않는다.

2. **재사용성 향상**  
   동일한 use case를 REST 외 다른 인터페이스에서 호출할 때 `MemberCommand`를 그대로 사용해 테스트/통합이 쉬워진다.

3. **검증 및 변환 지점 명확화**  
   Request → Command 변환 구간에 공통 검증이나 값 객체 변환을 두어, 이후 계층에서는 검증된 데이터만 다룬다.

4. **테스트 편의성**  
   Command는 HTTP 의존성이 없어 단위 테스트에서 쉽게 생성·주입할 수 있다.

## Command로 도메인 객체를 만드는 구조는 타당한가?

타당하다. Command는 단지 입력값을 묶은 전달 객체이며, 실제 도메인 엔티티 생성/갱신 로직은 도메인 팩토리(`Member.create`)나 엔티티 메서드(`updateInformation`)가 담당한다. Command가 도메인 모델을 대체하지 않는다면 DDD 원칙에 어긋나지 않는다.

## 헥사고날 아키텍처 적용 포인트

- **포트/어댑터 구조**  
  `MemberRepository`가 도메인에서 바라보는 출력 포트이며, `MemberRepositoryAdapter` + `MemberJpaRepository` 조합이 출력 어댑터다. 입력 측에서는 `MemberController`가 HTTP 어댑터, `MemberService`가 입력 포트 역할을 수행해 use case를 노출한다.
- **In/Out Port 네이밍은 생략**  
  `InPort`, `OutPort`라는 이름을 직접 쓰지는 않았지만, 위 구조가 사실상 포트/어댑터 패턴을 충족한다. 필요하다면 아래처럼 인터페이스를 분리해 명시적으로 이름 붙일 수 있다.

  ```java
  // 입력 포트 예시
  public interface MemberUseCase {
      ResponseEntity<List<MemberInfo>> findAll(Pageable pageable);
      ResponseEntity<MemberInfo> create(MemberCommand command);
  }

  // 출력 포트 예시
  public interface MemberOutPort {
      Page<Member> findAll(Pageable pageable);
      Member save(Member member);
  }
  ```

## 클린 아키텍처 적용 포인트

- **의존성 방향**  
  Controller(Request) → Command → Application Service → Domain → Infrastructure 순으로 단방향 의존한다. 도메인 모델은 어떤 외부 계층에 대해서도 모르는 상태로 유지된다.
- **경계 명확화**  
  Request/Command 분리가 계층 간 DTO를 구분해 Presentation 변화가 Application/Domain으로 새어 나오지 못하게 한다. Infrastructure도 `MemberRepository` 포트를 통해서만 접근되므로 내부 규칙을 지킬 수 있다.

## 요약

- Request는 **프레젠테이션 전용**, Command는 **use-case 전용** DTO다.
- 두 DTO가 같은 필드를 갖더라도, 책임 분리는 계층 간 결합을 줄이고 확장성과 테스트 편의성을 확보한다.
- Command는 도메인 객체 생성에 필요한 데이터를 안전하게 캡슐화하여 애플리케이션 서비스가 도메인 로직을 호출하도록 돕는다.
