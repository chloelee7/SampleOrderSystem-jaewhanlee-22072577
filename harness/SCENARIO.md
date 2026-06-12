# Harness Scenario Guide

## 목적

운영자가 실제 업무 흐름을 따라 시스템을 수동 검증할 수 있는 시나리오를 제공한다.

---

## 환경 준비

```bash
# 더미 데이터 포함 실행
./gradlew run --args="--seed-dummy"
```

초기 데이터:
- 시료: S-001 ~ S-005 (5개)
- 주문: ORD-D001(RESERVED), ORD-D002(CONFIRMED), ORD-D003(RELEASE), ORD-D004(REJECTED)

---

## 시나리오 A: 재고 충분 주문 → 출하

1. `[2] 시료 주문` → 시료 ID: `S-001`, 고객명: `Test Lab`, 수량: `10`
2. `[3] 주문 승인/거절` → `[1] 승인` → 생성된 주문 ID 입력
   - 기대: CONFIRMED (S-001 재고 480 ≥ 10)
3. `[6] 출고 처리` → 해당 주문 ID 입력
   - 기대: RELEASE, S-001 재고 470

---

## 시나리오 B: 재고 부족 → 생산 → 출하

1. `[2] 시료 주문` → 시료 ID: `S-003`, 고객명: `Power Lab`, 수량: `40`
   - S-003 현재 재고: 30
2. `[3] 주문 승인/거절` → `[1] 승인` → 주문 ID 입력
   - 기대: PRODUCING (부족 10개, 생산잡 JOB-xxxx RUNNING 자동 시작)
3. `[5] 생산라인 조회` → `[3] 시간 경과 처리` → 경과 시간: `11`
   - 기대: 생산 완료 (ceil(10 / (0.92 × 0.9)) = 13개 계획, 0.8분 × 13 = 10.4분 → 11분)
   - 주문 CONFIRMED, S-003 재고 +10 = 40
4. `[6] 출고 처리` → 해당 주문 ID 입력
   - 기대: RELEASE, S-003 재고 -40 = 0

---

## 시나리오 C: 모니터링 확인

1. `[4] 모니터링` 실행
   - 주문 상태별 건수 확인
   - 시료별 재고 현황 (물리재고 / 할당 / 가용 / 미출고 / 상태)
   - 상태 표시: 여유 / 부족 / 고갈

---

## 시나리오 D: 주문 거절

1. `[2] 시료 주문` → 시료 ID: `S-002`, 고객명: `Reject Test`, 수량: `5`
2. `[3] 주문 승인/거절` → `[2] 거절` → 주문 ID 입력
   - 기대: REJECTED
   - 모니터링에서 REJECTED는 별도 집계 (`REJECTED(집계 제외): N`)

---

## 시나리오 E: FIFO 생산 큐

1. 시료 `S-003` 재고 0으로 만든 후 (출하로 소진)
2. 주문 A (qty=10) 접수 후 승인 → PRODUCING, JOB-xxxx RUNNING
3. 주문 B (qty=5) 접수 후 승인 → PRODUCING, JOB-yyyy WAITING
4. `[5] 생산라인 조회` → `[2] 생산 대기 큐` → JOB-yyyy 확인
5. 시간 경과 → 주문 A CONFIRMED, JOB-yyyy RUNNING
6. 추가 시간 경과 → 주문 B CONFIRMED

---

## 자동화 시나리오 (HarnessScenarioTest)

`./gradlew test` 실행 시 `HarnessScenarioTest`가 시나리오 B 전체를 자동 검증한다.
