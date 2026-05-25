# ---- Etapa 1: Build ----
FROM eclipse-temurin:17-jdk AS build

RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# ---- Etapa 2: Runtime ----
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=build /app/target/plataforma-educativa-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
