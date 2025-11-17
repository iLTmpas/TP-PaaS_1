FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/telemetry-backend.jar telemetry-backend.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","telemetry-backend.jar"]
