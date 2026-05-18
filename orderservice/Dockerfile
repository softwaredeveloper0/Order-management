FROM eclipse-temurin:17 AS builder

WORKDIR /app

COPY orderservice/target/*.jar app.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]