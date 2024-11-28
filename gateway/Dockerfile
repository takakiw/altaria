FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update -y && apt-get install -y curl

COPY target/gateway.jar /app/gateway.jar

ENV JVM_OPTS="-Xms128m -Xmx128m"

EXPOSE 10086

CMD ["sh", "-c", "java $JVM_OPTS -jar gateway.jar"]