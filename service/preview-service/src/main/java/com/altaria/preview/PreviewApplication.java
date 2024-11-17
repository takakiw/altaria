package com.altaria.preview;

import com.altaria.feign.client.FileServiceClient;
import com.altaria.feign.client.PreviewServiceClient;
import com.altaria.feign.config.FileServiceFeignConfig;
import com.altaria.feign.config.PreviewServiceFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(defaultConfiguration = {FileServiceFeignConfig.class},
        clients = {FileServiceClient.class})
public class PreviewApplication {
    public static void main(String[] args) {
        SpringApplication.run(PreviewApplication.class, args);
    }
}
