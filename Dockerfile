FROM openjdk:14.0
COPY . .
RUN ./gradlew assemble
RUN cp build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=staging", "-jar","/app.jar"]