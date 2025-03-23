FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

VOLUME /config

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/app.jar"]