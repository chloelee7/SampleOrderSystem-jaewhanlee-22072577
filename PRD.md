# PRD — 반도체 시료 생산주문관리 시스템

## 개요

반도체 시료(Sample)의 주문 접수부터 생산, 출하까지 전 과정을 관리하는 콘솔 기반 MVC 시스템이다.

## 사용자 역할

- **운영자**: 시료 등록, 주문 승인/거절, 생산 관리, 출하 처리를 수행하는 단일 사용자

## 핵심 기능 요구사항

### 1. 시료 관리
- 시료 등록: 시료 ID, 시료명, 평균 생산시간(분, double), 수율(0~1), 초기 재고 입력
- 시료 목록 조회 / 검색 (ID 또는 이름)

### 2. 주문 관리
- **주문 접수**: 시료 ID, 고객명, 수량 → RESERVED 상태로 등록 (ID 자동 생성 ORD-xxxx)
- **주문 승인/거절**:
  - 승인: 가용 재고 자동 판단
    - 충분 → CONFIRMED, allocatedQuantity = quantity
    - 부족 → PRODUCING, ProductionJob(WAITING) 자동 등록, 즉시 RUNNING 전환 (라인이 비어있으면)
  - 거절: RESERVED → REJECTED

### 3. 생산 관리
- 생산 대기(WAITING) / 현재 진행(RUNNING) 큐 조회 (총 생산시간·시작·완료예정 포함)
- 시간 경과 처리: 분(minutes) 입력 → `MutableTimeProvider.advanceMinutes()` + `synchronizeProductionLine()`
  - 입력값은 1분 이상이어야 함 (0 이하 입력 시 오류)
  - expectedEndAt <= now: RUNNING → COMPLETED, 재고 복구, 주문 CONFIRMED
  - 다음 WAITING → RUNNING (FIFO)
  - 여러 작업이 연속 완료 가능한 경우 일괄 처리 (while 루프)

### 4. 출하 관리
- CONFIRMED 주문 목록 출력 후 주문 ID 입력 → RELEASE 전환, 재고 감소

### 5. 모니터링
- 주문 상태별 건수 (RESERVED / PRODUCING / CONFIRMED / RELEASE)
- 상태별 주문 목록 출력 (REJECTED 제외)
- REJECTED는 별도 집계 (모니터링 주 집계 제외)
- 시료별 재고 현황 (물리재고 / 할당 / 가용 / 미출고 / 상태[여유/부족/고갈])

### 6. Dummy Data
- `--seed-dummy` 인수 또는 메뉴 [7]: 멱등적으로 5 samples + 4 orders + 1 job 생성

## 비즈니스 규칙

| 규칙 | 공식 |
|---|---|
| 가용 재고 | `stock - SUM(allocatedQty of CONFIRMED/PRODUCING orders)` |
| 계획 생산량 | `ceil(shortageQty / (yieldRate × 0.9))` |
| 총 생산시간 | `averageProductionTimeMinutes × plannedProductionQuantity` |
| 생산완료 재고복구 | `stock += shortageQuantity` |
| 생산완료 할당완성 | `order.allocatedQuantity += shortageQuantity → order.quantity` |

## 주문 상태 전이

```
RESERVED → CONFIRMED  (재고 충분 시 승인)
RESERVED → PRODUCING  (재고 부족 시 승인 → 생산 등록)
RESERVED → REJECTED   (거절)
PRODUCING → CONFIRMED (생산 완료)
CONFIRMED → RELEASE   (출하)
```

## 비기능 요구사항

- Java 17, Gradle, JUnit 5
- JSON 파일 기반 영속성 (Jackson 2.17.2 + jsr310)
- 콘솔 기반 출력 (printf 형식 테이블)
- 테스트 가능한 설계 (MutableTimeProvider)
- EOF 입력 시 graceful shutdown (ConsoleInputClosedException)
