# Use Gradle with JDK 17 for build stage
FROM gradle:7.6-jdk17-alpine as builder
WORKDIR /build

# Copy Gradle settings
COPY build.gradle settings.gradle /build/

# Debug Gradle file setup
RUN ls -l /build && cat /build/build.gradle

# Build application
COPY . /build
RUN gradle build -x test --parallel --info

# Final runtime image
FROM openjdk:17.0-slim
WORKDIR /app
COPY --from=builder /build/build/libs/*-SNAPSHOT.jar ./app.jar
RUN chown nobody:nogroup /app
USER nobody
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
