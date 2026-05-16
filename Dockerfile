FROM eclipse-temurin:25-jdk-alpine

WORKDIR /core

COPY build/libs/*.jar core.jar
COPY configurations/ configurations/
COPY geo/ geo/

EXPOSE 8080

ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "core.jar"]