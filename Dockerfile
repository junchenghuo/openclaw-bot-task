FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /opt/task-center
COPY --from=build /app/target/task-center.jar ./task-center.jar
EXPOSE 18080
ENTRYPOINT ["java", "-jar", "task-center.jar"]
