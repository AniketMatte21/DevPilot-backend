# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Force Java memory limits
ENTRYPOINT ["java", "-Xmx300m", "-Xss512k", "-XX:+UseContainerSupport", "-jar", "app.jar"]