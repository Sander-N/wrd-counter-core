FROM openjdk:20-jdk-slim as build

COPY . .

RUN ./gradlew build

FROM openjdk:20-jdk-slim

#Copy application jar to the image
COPY /build/libs/wrd-counter-0.0.1-SNAPSHOT.jar .

EXPOSE 9000

ENTRYPOINT java -jar wrd-counter-0.0.1-SNAPSHOT.jar