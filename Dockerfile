FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/plataforma-educativa-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/efs
ENTRYPOINT ["java","-jar","/app.jar"]