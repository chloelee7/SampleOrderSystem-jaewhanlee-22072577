# Requirements Mapping

PDF 과제 요구사항과 구현 위치·테스트 위치의 1:1 대응표.

## 미션1 — PoC

| 항목 | Repository |
|---|---|
| MVC 스켈레톤 | ConsoleMVC-jaewhanlee-22072577 |
| 데이터 영속성 | DataPersistence-jaewhanlee-22072577 |
| 데이터 모니터링 Tool | DataMonitor-jaewhanlee-22072577 |
| Dummy 데이터 생성 Tool | DummyDataGenerator-jaewhanlee-22072577 |

## 미션2 — 프로젝트 기능 명세

### 시료 관리

| 요구사항 | 구현 | 테스트 |
|---|---|---|
| 시료 등록 (ID / 이름 / 평균생산시간 / 수율 / 초기재고) | `SampleController.register()`, `SampleService.registerSample()` | `SampleAndOrderServiceTest` |
| 시료 목록 조회 (재고 수량 포함) | `SampleController`, `TablePrinter.printSamples()` | `SampleAndOrderServiceTest` |
| 시료 이름 검색 | `SampleService.searchSamples()` | `SampleAndOrderServiceTest` |

### 시료 주문

| 요구사항 | 구현 | 테스트 |
|---|---|---|
| 주문 접수 → RESERVED (시료ID / 고객명 / 수량) | `OrderController.reserve()`, `OrderService.reserveOrder()` | `SampleAndOrderServiceTest` |

### 주문 승인/거절

| 요구사항 | 구현 | 테스트 |
|---|---|---|
| RESERVED 목록 조회 | `ApprovalController`, `OrderService.listReservedOrders()` | `WorkflowServiceTest` |
| 승인 — 재고 충분 → CONFIRMED | `OrderService.approveOrder()` | `WorkflowServiceTest.approvesOrderAsConfirmedWhenAvailableStockIsEnough` |
| 승인 — 재고 부족 → PRODUCING + 생산잡 자동 등록 | `OrderService.approveOrder()`, `ProductionService.enqueueProduction()` | `WorkflowServiceTest.approvesOrderAsProducingAndCreatesRunningProductionJobWhenStockIsShort` |
| 거절 → REJECTED | `OrderService.rejectOrder()` | `WorkflowServiceTest.rejectsReservedOrder` |
| 가용 재고 = 물리재고 − CONFIRMED/PRODUCING 할당량 | `OrderService.availableStock()` | `WorkflowServiceTest` |

### 생산라인

| 요구사항 | 구현 | 테스트 |
|---|---|---|
| 생산 현황 (RUNNING 작업 정보) | `ProductionController`, `TablePrinter.printProductionJobs()` | `HarnessScenarioTest` |
| 대기 큐 조회 (FIFO 순서) | `ProductionController`, `ProductionJobRepository` | `WorkflowServiceTest.processesProductionJobsInFifoOrderAndConfirmsOrders` |
| 시간 경과 처리 | `ProductionController`, `ProductionService.synchronizeProductionLine()` | `WorkflowServiceTest`, `HarnessScenarioTest` |
| 실 생산량 = ceil(부족분 / (수율 × 0.9)) | `ProductionCalculator.plannedProductionQuantity()` | `ProductionCalculatorTest` |
| 총 생산시간 = 평균시간 × 실생산량 | `ProductionCalculator.totalProductionTimeMinutes()` | `ProductionCalculatorTest` |
| 생산완료 → PRODUCING → CONFIRMED, 재고 증가 | `ProductionService.synchronizeProductionLine()` | `WorkflowServiceTest`, `HarnessScenarioTest` |
| FIFO 스케줄링 | `ProductionService.startNextWaitingJobIfIdle()` | `WorkflowServiceTest.processesProductionJobsInFifoOrderAndConfirmsOrders` |
| 여러 작업 일괄 완료 (충분한 시간 경과 시) | `ProductionService.synchronizeProductionLine()` (while 루프) | `WorkflowServiceTest.completesMultipleJobsWhenSufficientTimeElapses` |

### 출고 처리

| 요구사항 | 구현 | 테스트 |
|---|---|---|
| CONFIRMED 목록 조회 | `ReleaseController`, `OrderRepository` | `WorkflowServiceTest` |
| 출고 실행 → RELEASE, 재고 차감 | `ReleaseService.releaseOrder()`, `ReleaseController` | `WorkflowServiceTest.releasesConfirmedOrderAndDecreasesStock` |

### 모니터링

| 요구사항 | 구현 | 테스트 |
|---|---|---|
| 상태별 주문 수 (RESERVED/CONFIRMED/PRODUCING/RELEASE) | `MonitoringService.createSnapshot()` | `WorkflowServiceTest` |
| REJECTED 집계 제외 (별도 카운트) | `MonitoringService` (ACTIVE_STATUSES 미포함) | `WorkflowServiceTest.rejectsReservedOrder` |
| 시료별 재고 여유/부족/고갈 상태 | `MonitoringService.inventoryStatus()`, `InventoryStatus` enum | `WorkflowServiceTest` |

### 더미 데이터 / 영속성

| 요구사항 | 구현 | 테스트 |
|---|---|---|
| 더미 데이터 생성 (멱등) — `--seed-dummy` | `DummyDataService.generateIfMissing()` | `DummyDataServiceTest` |
| 더미 데이터 참조 무결성 | `DummyDataService` | `DummyDataServiceTest.dummyDataJobsReferenceExistingOrdersAndSamples` |
| JSON 파일 영속성 (재시작 후 유지) | `JsonFileStore`, `JsonObjectStore`, `File*Repository` | `FileRepositoryTest` |
| 커스텀 데이터 디렉터리 — `--data-dir` | `Application.run()` | — |

## 미션2 — 주안점

| 주안점 | 구현 위치 |
|---|---|
| CLAUDE.md / PRD.md 등 문서 관리 | `CLAUDE.md`, `PRD.md`, `README.md`, `ARCHITECTURE.md`, `TEST_PLAN.md` |
| Harness 도입 | `app/HarnessScenarioTest.java`, `harness/SCENARIO.md` |
| Test | 26개 단위/통합 테스트 (`./gradlew test`) |
| CleanCode | record 도메인 모델, 레이어 분리 (controller/service/repository/persistence) |
| Commit 이력 | 10개 의미있는 커밋 (`git log --oneline`) |
