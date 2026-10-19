# Multi-stage Dockerfile for RoadResQ Platform
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
VOLUME /tmp
COPY --from=build /app/target/roadresq-backend-1.0.0.jar app.jar
RUN mkdir -p uploads/problems data

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=mysql
ENV PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]
