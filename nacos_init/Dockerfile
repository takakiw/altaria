FROM ubuntu:22.04

WORKDIR /app

VOLUME ["/etc/nacos/data", "/etc/nacos/script"]

COPY ./data/nacos-config.zip /etc/nacos/data/nacos-config.zip

COPY ./script/nacos_init.sh /etc/nacos/script/nacos_init.sh

RUN apt-get update && apt-get install -y curl

RUN chmod +x /etc/nacos/script/nacos_init.sh

CMD ["/bin/bash", "/etc/nacos/script/nacos_init.sh", "/etc/nacos/data/nacos-config.zip"]
