FROM openjdk:17-jdk-slim

WORKDIR /app

RUN sed -i 's@/deb.debian.org/@/ftp.cn.debian.org/@g' /etc/apt/sources.list && \
    sed -i 's@/security.debian.org/@/ftp.cn.debian.org/@g' /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends ffmpeg && \
    apt-get clean && rm -rf /var/lib/apt/lists/*


COPY target/file-service.jar /app/file-service.jar

ENV JVM_OPTS="-Xms256m -Xmx256m"

VOLUME ["/temp/file/cover", "/temp/file/union"]

EXPOSE 8082

CMD ["sh", "-c", "java $JVM_OPTS -jar file-service.jar"]
