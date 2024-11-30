package com.altaria.gateway;


import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@SpringBootApplication
@ConditionalOnNacosDiscoveryEnabled
@RefreshScope
public class gatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(gatewayApplication.class, args);
    }
}
