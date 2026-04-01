FROM eclipse-temurin:25-jdk-alpine

WORKDIR /core

COPY build/libs/*.jar core.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "core.jar"]