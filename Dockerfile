FROM openjdk:17-jdk-slim AS build
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve

COPY src src
RUN ./mvnw package -DskipTests

FROM openjdk:17-jdk-slim
COPY --from=build target/*.jar demo.jar

RUN mkdir -p /app/files

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "demo.jar"]