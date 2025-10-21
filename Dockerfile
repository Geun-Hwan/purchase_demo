# Stage 1: Build
FROM openjdk:21-jdk-slim AS builder

WORKDIR /app

# Copy Gradle wrapper and configuration first to leverage Docker cache
COPY gradlew .
COPY gradle gradle/
COPY build.gradle .
COPY settings.gradle .

# Copy sub-projects' build.gradle files
COPY example-app/build.gradle example-app/
COPY purchase-framework-core/build.gradle purchase-framework-core/
COPY payment-gateway-toss/build.gradle payment-gateway-toss/
COPY notification-service-kakao/build.gradle notification-service-kakao/

# Download dependencies - this layer will be cached if build.gradle files don't change
RUN ./gradlew dependencies

# Copy all source code
COPY example-app example-app/
COPY purchase-framework-core purchase-framework-core/
COPY payment-gateway-toss payment-gateway-toss/
COPY notification-service-kakao notification-service-kakao/

# Build the application
RUN ./gradlew clean build -x test

# Stage 2: Run
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/example-app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]