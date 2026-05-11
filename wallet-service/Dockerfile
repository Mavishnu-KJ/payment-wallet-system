FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the pre-built JAR (from local build)
COPY target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]