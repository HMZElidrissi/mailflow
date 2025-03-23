FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar
COPY src/main/resources/bootstrap.yml /app/bootstrap.yml

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.config.location=classpath:/,file:/app/bootstrap.yml"]