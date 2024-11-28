FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/space-service.jar /app/space-service.jar

ENV JVM_OPTS="-Xms256m -Xmx256m"

EXPOSE 8084

CMD ["sh", "-c", "java $JVM_OPTS -jar space-service.jar"]