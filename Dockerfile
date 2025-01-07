# Use Java 17 JRE as the base image
FROM openjdk:17-jdk-slim

# Copy the JAR file from the build output to the container
# The build.gradle produces the JAR file under build/libs/
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]