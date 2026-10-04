# build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S pokethnos && adduser -S pokethnos -G pokethnos
COPY --from=build /app/target/*.jar app.jar
USER pokethnos
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
