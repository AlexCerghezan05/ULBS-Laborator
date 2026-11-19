# D-023: build cu Maven, rulare pe JRE 21. Testele rulează separat (profilul test din Compose).
FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21.0.13_9-jre-alpine
# D-024: aplicația rulează ca utilizator non-root.
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=build /app/target/p21-backend-0.0.1-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
