# Purchase/Payment/Notification Framework

## 🚀 프로젝트 소개

`purchase-framework`는 복잡한 구매, 주문, 결제 및 알림 처리를 효율적으로 관리하기 위한 Spring Boot 기반의 멀티 모듈 프레임워크입니다. 이 프레임워크는 핵심 비즈니스 로직을 추상화하고, 다양한 결제 게이트웨이 및 알림 서비스 구현체를 플러그인 방식으로 유연하게 통합할 수 있도록 설계되었습니다. `example-app`은 이 프레임워크의 강력한 기능을 활용하여 구축된 실제 애플리케이션의 예시로, 프레임워크의 사용법과 확장성을 보여줍니다.

## 📦 모듈 구조

이 프로젝트는 다음과 같은 멀티 모듈 구조로 구성되어 있습니다:

-   **`purchase-framework-core`**: 프레임워크의 핵심 모듈입니다.
    -   `PaymentGateway`, `NotificationInterface`와 같은 핵심 인터페이스를 정의하여 결제 및 알림 서비스의 추상화를 제공합니다.
    -   `Order`, `Product`, `User` 등 모든 구매/결제/알림 관련 애플리케이션에서 공통적으로 사용될 수 있는 추상화된 엔티티 및 DTO(`ApiResponse`, `PaymentResponse` 등)를 포함합니다. 이 엔티티들은 프레임워크의 기반을 제공하며, 실제 애플리케이션에서는 비즈니스 요구사항에 맞춰 확장하여 사용하도록 설계되었습니다.

-   **`payment-gateway-toss`**: `PaymentGateway` 인터페이스의 토스 페이먼츠 구현체 모듈입니다.
    -   토스 페이먼츠 API 연동 로직을 포함하며, 새로운 결제 게이트웨이를 추가할 때 참고할 수 있는 구현 예시를 제공합니다.

-   **`notification-service-kakao`**: `NotificationInterface` 인터페이스의 카카오 알림 구현체 모듈입니다.
    -   카카오 알림톡 API 연동 로직을 포함하며, 새로운 알림 서비스를 추가할 때 참고할 수 있는 구현 예시를 제공합니다.

-   **`example-app`**: 프레임워크를 사용하는 방법을 보여주는 예제 애플리케이션입니다.
    -   사용자 인증, 상품 관리, 주문 처리, 결제 및 알림 기능을 포함하며, `purchase-framework-core`, `payment-gateway-toss`, `notification-service-kakao` 모듈을 의존성으로 사용합니다.

## ✨ 주요 기능

-   **사용자 인증**: JWT 기반의 안전한 로그인 및 회원가입 기능
-   **상품 관리**: 상품 등록, 조회, 재고 관리 기능
-   **주문 관리**: 상품 주문, 주문 내역 조회 및 상태 관리
-   **결제 처리**: 플러그인 가능한 `PaymentGateway` 인터페이스를 통한 유연한 결제 연동 (예: 토스 페이먼츠 구현체 제공)
-   **알림 처리**: 플러그인 가능한 `NotificationInterface` 인터페이스를 통한 다양한 알림 서비스 연동 (예: 카카오 알림톡 구현체 제공)
-   **보안**: Spring Security와 JWT를 활용한 강력한 API 보안
-   **캐싱**: Redis를 활용한 고성능 데이터 캐싱
-   **메시징**: Apache Kafka를 활용한 비동기 메시징 및 이벤트 기반 아키텍처 지원

## 🛠️ 사용 기술

-   **백엔드**: Java 21, Spring Boot 3.2.0, Spring Security, Spring Data JPA
-   **데이터베이스**: H2 (로컬 개발 및 테스트), MySQL (Docker 환경 및 프로덕션)
-   **메시징 큐**: Apache Kafka
-   **캐시**: Redis
-   **결제 연동**: Toss Payments API
-   **알림 연동**: Kakao Alimtalk API
-   **빌드 도구**: Gradle (멀티 모듈)
-   **컨테이너**: Docker, Docker Compose

## 🚀 시작하기

### 전제 조건

-   Java Development Kit (JDK) 21 이상
-   Gradle 8.x 이상
-   Docker 및 Docker Compose

### 🏗️ 프로젝트 빌드

프로젝트 루트에서 다음 명령어를 실행하여 모든 모듈을 빌드합니다:

```bash
./gradlew clean build -x test
```
(테스트를 제외하고 빌드하여 시간을 단축합니다. 필요시 `-x test`를 제거할 수 있습니다.)

### 💻 로컬 개발 환경 설정 (example-app 기준)

1.  **저장소 클론**:
    ```bash
    git clone <저장소_URL>
    cd purchase_demo
    ```
2.  **환경 변수 설정**:
    `.env.example` 파일을 참조하여 `.env` 파일을 생성하고 필요한 환경 변수(예: `KAKAO_TOKEN`, `TOSS_SECRET_KEY` 등)를 설정합니다.
3.  **서비스 실행 (로컬)**:
    로컬에 Kafka, Redis가 설치되어 있고 실행 중인 경우, `example-app` 모듈을 직접 실행할 수 있습니다.
    ```bash
    cd example-app
    ./gradlew bootRun
    ```
    **참고**: `application.yml`에 H2 데이터베이스, `localhost` 기반 Redis/Kafka 설정이 기본으로 되어 있습니다. VSCode에서 실행 시 H2 데이터베이스를 사용합니다.

### 🐳 Docker 개발 환경 설정 (example-app 기준)

1.  **Docker 이미지 빌드 및 서비스 실행**:
    프로젝트 루트에서 다음 명령어를 실행하여 Docker 이미지를 빌드하고 모든 서비스를 Docker Compose로 실행합니다.
    ```bash
    docker-compose up --build -d
    ```
    *   `--build`: 변경 사항이 있을 경우 이미지를 다시 빌드합니다.
    *   `-d`: 백그라운드에서 서비스를 실행합니다.
2.  **애플리케이션 로그 확인**:
    애플리케이션이 정상적으로 시작되었는지 로그를 확인합니다.
    ```bash
    docker-compose logs -f purchase-framework-app
    ```
    **참고**: `application-docker.yml`에 MySQL 데이터베이스, Kafka, Redis 서비스 이름 기반 설정이 되어 있습니다. Docker 환경에서 실행 시 MySQL 데이터베이스를 사용합니다.

## 💡 프레임워크 사용법 및 확장성

이 프레임워크는 높은 확장성을 제공하여 다양한 비즈니스 요구사항에 맞춰 커스터마이징할 수 있습니다.

### 새로운 프로젝트에서 프레임워크 사용하기

새로운 Spring Boot 프로젝트에서 이 프레임워크를 사용하려면 다음 단계를 따릅니다:

1.  **새로운 Spring Boot 프로젝트 생성**:
    Gradle 기반의 새로운 Spring Boot 프로젝트를 생성합니다.

2.  **프레임워크 모듈 의존성 추가**:
    새 프로젝트의 `settings.gradle`에 프레임워크 모듈을 포함하고, `build.gradle` 파일에 필요한 프레임워크 모듈을 의존성으로 추가합니다. (현재는 로컬 경로를 사용하지만, 실제 배포 시에는 Maven Central 등에 게시된 아티팩트를 사용합니다.)

    ```gradle
    // settings.gradle (새로운 프로젝트)
    include 'purchase-framework-core'
    project(':purchase-framework-core').projectDir = file('../path/to/your/purchase-framework/purchase-framework-core')

    // build.gradle (새로운 프로젝트)
    dependencies {
        // 핵심 프레임워크 모듈
        implementation project(':purchase-framework-core')

        // (선택 사항) 토스 페이먼츠 구현체
        implementation project(':payment-gateway-toss')

        // (선택 사항) 카카오 알림 구현체
        implementation project(':notification-service-kakao')

        // 기타 필요한 Spring Boot 스타터 등
        implementation 'org.springframework.boot:spring-boot-starter-web'
        // ...
    }
    ```

3.  **서비스 로직 개발**:
    새 프로젝트의 서비스 클래스에서 `PaymentGateway` 및 `NotificationInterface`와 같은 프레임워크 인터페이스를 주입받아 사용합니다. Spring의 DI 컨테이너가 클래스패스에 있는 구현체를 자동으로 연결해 줄 것입니다.

    ```java
    @Service
    @RequiredArgsConstructor
    public class MyCustomOrderService {
        private final PaymentGateway paymentGateway;
        private final NotificationInterface notificationService;

        public void processOrder(Order order) {
            // 결제 요청
            PaymentResponse paymentResponse = paymentGateway.requestPayment(order);
            // 알림 발송
            notificationService.sendPaymentSuccessNotification(order.getId(), "주문이 완료되었습니다.");
            // ...
        }
    }
    ```

4.  **환경 설정**:
    새 프로젝트의 `application.yml` 또는 `application.properties` 파일에 결제/알림 서비스에 필요한 API 키, 리다이렉트 URL 등 환경 변수를 설정합니다.

### 엔티티 확장 가이드라인

`purchase-framework-core` 모듈의 엔티티(`User`, `Product`, `Order`)는 프레임워크의 핵심 개념을 정의하는 기반 역할을 합니다. 실제 애플리케이션의 특정 비즈니스 요구사항에 맞춰 이 엔티티들을 확장하여 사용할 수 있습니다.

예를 들어, `User` 엔티티에 추가적인 필드(예: `address`, `phoneNumber`)가 필요하다면, 새로운 애플리케이션 모듈에서 `User` 엔티티를 상속받아 확장하거나, `@MappedSuperclass`를 활용하여 공통 필드를 공유하고 각 애플리케이션에서 별도의 엔티티를 정의할 수 있습니다.

```java
// MyUser.java (새로운 애플리케이션 모듈)
@Entity
public class MyUser extends User { // purchase-framework-core.entity.User 상속
    private String address;
    private String phoneNumber;
    // ... 추가 필드 및 메서드
}
```

### 새로운 결제/알림 서비스 추가

프레임워크는 `PaymentGateway` 및 `NotificationInterface` 인터페이스를 통해 새로운 결제 게이트웨이나 알림 서비스를 쉽게 통합할 수 있도록 설계되었습니다.

1.  **인터페이스 구현**:
    새로운 결제/알림 서비스 모듈을 생성하고, `PaymentGateway` 또는 `NotificationInterface` 인터페이스를 구현하는 클래스를 작성합니다.
2.  **Spring Bean 등록**:
    구현체 클래스에 `@Service` 또는 `@Component` 어노테이션을 붙여 Spring Bean으로 등록합니다.
3.  **의존성 추가**:
    `example-app`과 같은 애플리케이션 모듈의 `build.gradle`에 새로 생성한 서비스 모듈의 의존성을 추가합니다.
4.  **설정**:
    `application.yml`에 새로운 서비스에 필요한 설정(API 키, 엔드포인트 등)을 추가합니다.

## 📚 API 엔드포인트 및 테스트

`example-app`을 실행한 후 다음 방법을 통해 API를 확인하고 테스트할 수 있습니다:

1.  **Swagger UI**:
    애플리케이션 실행 후 웹 브라우저에서 `http://localhost:8080/swagger-ui/index.html` 에 접속하여 API 문서를 확인하고 테스트할 수 있습니다.

2.  **Postman Collection**:
    프로젝트 루트에 있는 `purchase_demo_postman_collection.json` 파일을 Postman에 임포트하여 미리 정의된 API 요청들을 사용하여 테스트할 수 있습니다.

## 🤝 기여

기여를 환영합니다! 버그 리포트, 기능 제안 또는 코드 기여는 언제든지 환영합니다.

## 📄 라이선스

이 프로젝트는 [라이선스 이름] 라이선스 하에 배포됩니다. (예: MIT License)