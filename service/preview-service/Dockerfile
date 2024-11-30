FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/preview-service.jar /app/preview-service.jar

ENV JVM_OPTS="-Xms256m -Xmx256m"

EXPOSE 8086

CMD ["sh", "-c", "java $JVM_OPTS -jar preview-service.jar"]