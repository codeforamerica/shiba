FROM openjdk:14.0
COPY . .
RUN ./gradlew assemble
RUN cp build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]