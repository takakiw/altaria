FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/note-service.jar /app/note-service.jar

ENV JVM_OPTS="-Xms256m -Xmx256m"

EXPOSE 8085

CMD ["sh", "-c", "java $JVM_OPTS -jar note-service.jar"]