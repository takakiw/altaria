package com.altaria.file;



import com.alibaba.cloud.seata.feign.SeataFeignClientAutoConfiguration;
import com.altaria.feign.client.SpaceServiceClient;
import com.altaria.feign.config.SpaceServiceFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
@SpringBootApplication(exclude = {SeataFeignClientAutoConfiguration.class}) // 排除seata的feign配置，使用自定义的配置开启熔断降级
@EnableAsync
@ServletComponentScan
@EnableFeignClients(defaultConfiguration = SpaceServiceFeignConfig.class, clients = {SpaceServiceClient.class})
public class FileApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
