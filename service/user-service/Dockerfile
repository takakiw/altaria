FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/user-service.jar /app/user-service.jar

ENV JVM_OPTS="-Xms256m -Xmx256m"

EXPOSE 8081

CMD ["sh", "-c", "java $JVM_OPTS -jar user-service.jar"]