FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
RUN mkdir -p /app/efs
ENTRYPOINT ["java","-jar","/app.jar"]
