FROM openjdk:8-alpine

COPY target/uberjar/sample.jar /sample/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/sample/app.jar"]
