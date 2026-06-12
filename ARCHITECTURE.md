# ARCHITECTURE.md

## 레이어 구조

```
Controller → Service → Repository → JsonFileStore / JsonObjectStore (data/*.json)
                ↑
           Domain Model (records, enums)
                ↑
           Util (IdGenerator, TimeProvider, ProductionCalculator, ValidationUtils)
```

## 의존성 흐름

```
Main → Application
         └── MainController
              ├── SampleController    → SampleService       → SampleRepository     → JsonFileStore
              ├── OrderController     → OrderService        → OrderRepository      → JsonFileStore
              │                             └── ProductionService (순환참조 방지: 생성자 주입)
              ├── ApprovalController  → OrderService
              ├── ProductionController→ ProductionService   → ProductionJobRepository → JsonFileStore
              │                             ├── SampleRepository
              │                             └── OrderRepository
              ├── ReleaseController   → ReleaseService      → OrderRepository / SampleRepository
              └── MonitoringController→ MonitoringService   → 3 repositories
```

## 도메인 모델

### Sample (record)
```
id | name | averageProductionTimeMinutes (double) | yieldRate (double) | stockQuantity (int)
withStockQuantity(int): Sample
```

### Order (record)
```
id | sampleId | customerName | quantity | status | createdAt | updatedAt
   | allocatedQuantity | rejectedAt | releasedAt
Factory: reserve() | reject() | toProducing() | toConfirmed() | toRelease()
```

### ProductionJob (record)
```
id | orderId | sampleId | shortageQuantity | plannedProductionQuantity
   | averageProductionTimeMinutes | totalProductionTimeMinutes
   | status | createdAt | startedAt | expectedEndAt | completedAt
Factory: waiting() | start() | complete()
```

### SequenceState (record)
```
nextOrderNumber | nextProductionJobNumber
Factory: initial() → (1, 1)
```

## 주문 상태 전이

```
RESERVED ──승인(재고 OK)──→ CONFIRMED ──출하──→ RELEASE
RESERVED ──승인(재고 X)──→ PRODUCING ──생산완료→ CONFIRMED ──출하──→ RELEASE
RESERVED ──거절──────────→ REJECTED
```

## 가용 재고 계산

```
availableStock = sample.stockQuantity
    - SUM(order.allocatedQuantity WHERE order.sampleId == sampleId
          AND order.status IN [CONFIRMED, PRODUCING])
```

## 생산 완료 처리

```
sample.stockQuantity += job.shortageQuantity   (부족분만큼 재고 복구)
order.allocatedQuantity += job.shortageQuantity (할당량 완성)
order.status → CONFIRMED
```

## 영속성

- `JsonFileStore<T>`: 배열 저장 (samples/orders/production_jobs)
- `JsonObjectStore<T>`: 단일 객체 저장 (sequences)
- ObjectMapper: JavaTimeModule 등록, `WRITE_DATES_AS_TIMESTAMPS=false`
- 파일 없거나 비어있으면 빈 리스트 / Optional.empty() 반환

## 시간 관리

- `MutableTimeProvider`: 시간을 멈추거나 앞당길 수 있는 TimeProvider 구현체
- 생산 라인 시간 경과: ProductionController.advanceTime() → timeProvider.advanceMinutes() → productionService.synchronizeProductionLine()

## 빌드 / 실행

```bash
./gradlew run                          # 일반 실행
./gradlew run --args="--seed-dummy"    # 더미 데이터 포함 실행
./gradlew run --args="--data-dir custom" # 커스텀 데이터 디렉터리
./gradlew test                         # 테스트
```
