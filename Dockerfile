# Stage 1: Build the application using Gradle
FROM gradle:8.4.0-jdk17 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY src ./src

RUN chmod +x ./gradlew
RUN ./gradlew build --no-daemon

# Stage 2: Create the final, smaller image
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
