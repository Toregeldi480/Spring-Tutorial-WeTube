FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y openjdk-21-jre-headless ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY /target/*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]