FROM openjdk:8-alpine

COPY target/uberjar/socn.jar /socn/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/socn/app.jar"]
