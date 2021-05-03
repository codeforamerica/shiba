FROM openjdk:16.0
COPY . .
RUN ./gradlew assemble
RUN cp build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/app.jar"]