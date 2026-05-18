FROM eclipse-temurin:17 AS builder

WORKDIR /app

COPY . .


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]