# SampleOrderSystem-jaewhanlee-22072577

반도체 시료 생산주문관리 시스템 — Console MVC 본 프로젝트

## 실행

```bash
./gradlew run

# 더미 데이터 포함 실행
./gradlew run --args="--seed-dummy"

# 커스텀 데이터 디렉터리
./gradlew run --args="--data-dir custom_data"
```

## 테스트

```bash
./gradlew test
```

## 메뉴 구조

```
[1] 시료 관리      - 시료 등록 / 목록 조회 / 검색
[2] 시료 주문      - 주문 접수(RESERVED)
[3] 주문 승인/거절  - 접수 목록 조회 후 승인(재고 자동 판단) / 거절
[4] 모니터링       - 주문 현황 + 재고 현황 대시보드
[5] 생산라인 조회  - 현재 생산 현황 / 대기 큐 / 시간 경과 처리
[6] 출고 처리      - CONFIRMED 주문 출고 → RELEASE
[7] Dummy Data 생성
[0] 종료
```

## 기술 스택

- Java 17
- Gradle
- JUnit 5
- Jackson Databind 2.17.2 + jackson-datatype-jsr310 (JSON 파일 영속성)
