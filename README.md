# purchase_demo

## 프로젝트 소개

`purchase_demo`는 구매, 주문, 결제 및 상품 관리를 위한 Spring Boot 기반의 애플리케이션입니다. 사용자 인증, 상품 조회 및 생성, 주문 처리, 그리고 토스 페이먼츠를 통한 결제 기능을 제공합니다.

## 주요 기능

- **사용자 인증**: JWT 기반의 로그인 및 회원가입 기능
- **상품 관리**: 상품 등록, 조회
- **주문 관리**: 상품 주문, 주문 내역 조회
- **결제 처리**: 토스 페이먼츠를 이용한 결제 연동, Kafka를 통한 결제 메시지 처리
- **보안**: Spring Security와 JWT를 활용한 API 보안
- **캐싱**: Redis를 활용한 데이터 캐싱
- **메시징**: Kafka를 활용한 비동기 메시징

## 사용 기술

- **백엔드**: Java 21, Spring Boot, Spring Security, Spring Data JPA
- **데이터베이스**: 로컬 개발 환경을 위한 데이터베이스 (예: H2, PostgreSQL 등)
- **메시징 큐**: Apache Kafka (로컬 환경)
- **캐시**: Redis (로컬 환경)
- **결제 연동**: Toss Payments API
- **빌드 도구**: Gradle
- **컨테이너**: Docker, Docker Compose

## 시작하기

### 전제 조건

- Java 17
- Docker 및 Docker Compose

### 설치 및 실행

1.  **저장소 클론**:
    ```bash
    git clone <저장소_URL>
    cd purchase_demo
    ```
2.  **환경 설정**:
    `.env.example` 파일을 참조하여 `.env` 파일을 생성하고 필요한 환경 변수(예: 데이터베이스 연결 정보, Kafka 설정, Redis 설정, 토스 페이먼츠 API 키 등)를 설정합니다.
    **참고**: 데이터베이스, Redis, Kafka는 기본적으로 로컬 환경에서 실행되도록 설정되어 있습니다.
3.  **애플리케이션 빌드 및 실행**:
    Docker Compose를 사용하여 애플리케이션과 필요한 서비스(Kafka, Redis 등)를 함께 실행합니다.
    ```bash
    docker-compose up --build
    ```
    또는 Gradle을 사용하여 로컬에서 실행할 수 있습니다.
    ```bash
    ./gradlew bootRun
    ```
    **참고**: Gradle로 실행 시, Redis, 데이터베이스, Kafka는 로컬에 설치되어 있어야 합니다.

## API 엔드포인트 (주요 컨트롤러)

- `AuthController`: 사용자 인증 관련 API (로그인, 회원가입)
- `ProductController`: 상품 관련 API (상품 조회, 등록)
- `OrderController`: 주문 관련 API (주문 생성, 조회)
- `PaymentController`: 결제 관련 API (결제 요청, 콜백 처리)

## 기능 테스트

애플리케이션 기능 테스트는 다음 두 가지 방법으로 수행할 수 있습니다:

1.  **Swagger UI**:
    애플리케이션 실행 후 웹 브라우저에서 `http://localhost:8080/swagger-ui/index.html` 에 접속하여 API 문서를 확인하고 테스트할 수 있습니다.

2.  **Postman Collection**:
    `purchase_demo_postman_collection.json` 파일을 Postman에 임포트하여 미리 정의된 API 요청들을 사용하여 테스트할 수 있습니다.