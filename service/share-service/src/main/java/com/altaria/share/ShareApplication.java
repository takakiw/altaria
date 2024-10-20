package com.altaria.share;


import com.altaria.feign.client.FileServiceClient;
import com.altaria.feign.config.FileServiceFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(defaultConfiguration = FileServiceFeignConfig.class, clients = {FileServiceClient.class})
public class ShareApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShareApplication.class, args);
    }
}
