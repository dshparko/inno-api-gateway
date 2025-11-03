FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

COPY ApiGateway/pom.xml ./

COPY ApiGateway/src ./src
RUN mvn clean package -DskipTests=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/ApiGateway-0.0.1-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["java", "--enable-preview", "-jar", "/app/app.jar"]
