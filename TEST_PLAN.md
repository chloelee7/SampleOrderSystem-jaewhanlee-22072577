# TEST_PLAN.md

## 테스트 원칙

- **실패-우선**: 새 기능은 JUnit 실패 테스트로 시작한다.
- **임시 디렉터리**: `@TempDir`로 테스트 데이터 격리 (`data/` 오염 방지).
- **GREEN 커밋**: `./gradlew test` 통과 후 커밋한다.

## 단위 테스트

### ProductionCalculatorTest
- `plannedProductionQuantity(10, 0.92)` → 13
- `totalProductionTimeMinutes(0.8, 13)` → 10.4
- 잘못된 입력(0, 음수) → IllegalArgumentException

### SampleTest
- 유효한 Sample 생성 및 `withStockQuantity()` 확인
- 빈 이름, 음수 시간, 범위 외 yieldRate, 음수 재고 → 예외

### OrderTest
- `Order.reserve()`: RESERVED, allocatedQuantity=0, rejectedAt/releasedAt null 확인
- 상태 전이 및 타임스탬프 변경 확인 (toProducing → toConfirmed → toRelease)

### ProductionJobTest
- `waiting().start().complete()` 팩토리 체인 확인
- `expectedEndAt = startedAt + ceil(totalProductionTimeMinutes)`

### FileRepositoryTest (@TempDir)
- Sample 저장/재로드 라운드트립
- Order 타임스탬프 포함 직렬화/역직렬화
- ProductionJob FIFO 순서 유지
- 파일 없음/빈 파일 → 빈 컬렉션
- SequenceState 자동증가 영속성 확인

## 서비스 테스트

### SampleAndOrderServiceTest (@TempDir + MutableTimeProvider)
- 시료 등록 및 중복 ID 방지
- 시료 검색 (ID/이름)
- 주문 예약: ID 자동생성 (ORD-0001), RESERVED 상태
- 미등록 시료 주문 → 예외

### WorkflowServiceTest (@TempDir + MutableTimeProvider)
- 주문 거절: REJECTED 전환, 모니터링 집계 확인
- 재고 충분 시 승인: CONFIRMED, allocatedQuantity 설정, 가용재고 감소
- 재고 부족 시 승인: PRODUCING, ProductionJob(RUNNING) 생성
- 생산 FIFO: 첫 번째 잡 완료 → 두 번째 잡 시작
- 출하: CONFIRMED → RELEASE, 재고 감소

### DummyDataServiceTest (@TempDir)
- `generateIfMissing()` 멱등성: 2회 호출해도 5 samples / 4 orders / 2 jobs

## 통합 테스트

### HarnessScenarioTest (@TempDir + MutableTimeProvider)
- 더미 데이터 생성 → S-003 주문(qty=40, stock=30) → 승인(PRODUCING) → 시간 경과 11분 → `synchronizeProductionLine()` → CONFIRMED → 출하(RELEASE)
- RELEASE 주문 2건 확인 (기존 1건 + 신규 1건)

### MainControllerTest (@TempDir)
- EOF 입력 시 `ConsoleInputClosedException` 잡아서 graceful shutdown
- 종료 메시지 출력 확인

## 검증 명령

```bash
./gradlew test
./gradlew run --args="--seed-dummy"
```
