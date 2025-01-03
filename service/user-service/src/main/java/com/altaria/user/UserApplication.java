package com.altaria.user;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.altaria.feign.client.FileServiceClient;
import com.altaria.feign.config.FileServiceFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableAsync
@ServletComponentScan
@EnableScheduling
@ConditionalOnNacosDiscoveryEnabled
@RefreshScope
@EnableFeignClients(defaultConfiguration = FileServiceFeignConfig.class, clients = {FileServiceClient.class})
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}