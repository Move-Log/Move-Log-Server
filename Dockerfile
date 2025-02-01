# Use Gradle with JDK 17 for build stage
FROM gradle:7.6-jdk17-alpine as builder
WORKDIR /build

# Set TimeZone to Asia/Seoul for build stage
RUN apk add --no-cache tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

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

# Set TimeZone to Asia/Seoul for runtime stage
RUN apt-get update && apt-get install -y tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

COPY --from=builder /build/build/libs/*-SNAPSHOT.jar ./app.jar

# Ensure correct permissions
RUN chown nobody:nogroup /app
USER nobody
EXPOSE 8080

# Set the default TimeZone for the JVM
ENV TZ=Asia/Seoul
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.jar"]

