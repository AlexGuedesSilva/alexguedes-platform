FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY . .

RUN mvn -pl validate-service -am -DskipTests clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/validate-service/target/validate-service-1.0.0.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]