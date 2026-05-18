FROM eclipse-temurin:17 AS builder

WORKDIR /app

COPY target/*.jar app.jar

RUN mvn clean package -DskipTests

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]