# Simple Scheduling vs. Spring Batch

| 구분 | Simple Scheduling (@Scheduled + Service) | Spring Batch (Job/Step) |
| --- | --- | --- |
| **구조** | 스케줄러가 서비스 메서드 호출 | JobLauncher가 Job 실행 → Step(Tasklet/Chunk) 수행 |
| **구현 난이도** | 매우 간단, 추가 학습 거의 없음 | Job/Step 설계 및 Batch 스키마 필요, 초기 학습 비용 높음 |
| **실행 이력/추적** | 별도 구현 없으면 로그 외에 남지 않음 | BATCH_* 테이블에 Job/Step 상태·파라미터 자동 저장 |
| **오류/재시도 정책** | 직접 예외 처리 및 재시도 로직 구현해야 함 | 스킵/재시도/재개 정책을 설정으로 적용 가능 |
| **대량 처리 패턴** | 청크 처리/병렬화 등 수동 구현 필요 | Reader/Processor/Writer, Partitioning 등 내장 기능 |
| **수동 실행/파라미터** | 별도 로직 없으면 지원 어려움 | JobParameters로 특정 sellerId 등 조건 실행 용이 |
| **적합한 시나리오** | 소규모 데이터, 단순 작업, 실패 시 전체 재실행으로 충분 | 대량 데이터, 실행 이력 필요, 부분 재실행/수동 트리거 요구 |

> 정산 모듈처럼 판매자별 상태를 추적하고 필요 시 특정 파라미터로 다시 실행해야 하는 작업이라면 Spring Batch 구조가 운영·확장 측면에서 유리하지만, 단순·소규모 작업은 @Scheduled 방식이 더 가볍고 관리가 쉽다.
