# Stage 1: Build
FROM maven:3.9.9-amazoncorretto-21-alpine AS build

# Install Node.js and npm
RUN apk add --no-cache nodejs npm

WORKDIR /app

# Copy configuration files first
COPY pom.xml .
COPY package*.json ./

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -Pproduction -DskipTests

# Stage 2: Runtime
FROM amazoncorretto:21-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 9191

ENTRYPOINT ["java", "-jar", "app.jar"]