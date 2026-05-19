# Build stage: compiles the Spring Boot application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline
COPY src src
RUN ./mvnw -B -DskipTests clean package

# Runtime stage: lightweight JRE image to run the fat jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/stock-manager-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV DB_HOST=postgres \
    DB_USER=postgres \
    DB_PASSWORD=postgres
ENTRYPOINT ["java","-jar","/app/app.jar"]
