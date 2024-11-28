FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install ffmpeg -y

COPY target/file-service.jar /app/file-service.jar

ENV JVM_OPTS="-Xms256m -Xmx256m"

VOLUME ["/temp/file/cover", "/temp/file/union"]

EXPOSE 8082

CMD ["sh", "-c", "java $JVM_OPTS -jar file-service.jar"]