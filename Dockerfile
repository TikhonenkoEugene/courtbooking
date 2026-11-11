FROM eclipse-temurin:25-jdk AS build
ARG SERVICE
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY backend backend
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon :${SERVICE}:bootJar \
    && cp backend/${SERVICE}/build/libs/*.jar app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
