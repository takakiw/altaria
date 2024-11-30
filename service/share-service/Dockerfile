FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/share-service.jar /app/share-service.jar

ENV JVM_OPTS="-Xms256m -Xmx256m"

EXPOSE 8083

CMD ["sh", "-c", "java $JVM_OPTS -jar share-service.jar"]