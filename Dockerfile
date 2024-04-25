FROM maven:3.9-eclipse-temurin-17 AS build-stage
WORKDIR /app
COPY pom.xml /app/
RUN mvn dependency:go-offline
COPY src /app/src
RUN mvn clean package

FROM eclipse-temurin:17-jdk-alpine
COPY --from=build-stage /app/target/ExcursionBot-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "/app.jar"]
EXPOSE 80
