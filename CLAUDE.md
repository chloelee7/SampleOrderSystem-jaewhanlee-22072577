# Claude Working Guide

## Project Rules

- Java 17, Gradle, JUnit 5 기반 콘솔 애플리케이션이다.
- 웹 서버, GUI, 외부 API를 추가하지 않는다.
- 코드 식별자는 영어로 작성하고 콘솔 메시지는 한국어로 작성한다.
- 도메인 규칙 변경은 테스트를 먼저 작성한다.

## MVC Principles

- Controller는 메뉴 흐름만 담당한다.
- View는 입력과 출력만 담당한다.
- Service는 비즈니스 규칙을 담당한다.
- Repository는 저장과 조회만 담당한다.
- Persistence는 파일 I/O만 담당한다.

## Test Principles

- 새 기능은 실패하는 JUnit 테스트로 시작한다.
- 커밋 전 `./gradlew test`를 실행한다.
- 임시 디렉터리를 사용해 테스트 데이터를 격리한다.

## Commit Guide

- GREEN 상태에서 커밋한다.
- 리팩터링 커밋도 테스트 통과 후 남긴다.
- 권장 단위:
  - `docs: ...`
  - `test: ...`
  - `feat: ...`
  - `refactor: ...`
  - `fix: ...`

## Prohibited Work

- 콘솔 앱을 웹 앱으로 바꾸지 않는다.
- 상태 enum 이름 `RELEASE`를 다른 이름으로 바꾸지 않는다.
- REJECTED 주문을 정상 주문 모니터링 집계에 포함하지 않는다.
- 실제 사용자 data 디렉터리를 테스트에서 직접 사용하지 않는다.
