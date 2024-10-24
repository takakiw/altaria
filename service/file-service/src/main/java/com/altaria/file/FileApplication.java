package com.altaria.file;



import com.alibaba.cloud.seata.feign.SeataFeignClientAutoConfiguration;
import com.altaria.feign.client.SpaceServiceClient;
import com.altaria.feign.config.SpaceServiceFeignConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
@SpringBootApplication(exclude = {SeataFeignClientAutoConfiguration.class}) // 排除seata的feign配置，使用自定义的配置开启熔断降级
@EnableAsync
@ServletComponentScan
@EnableFeignClients(defaultConfiguration = SpaceServiceFeignConfig.class, clients = {SpaceServiceClient.class})
public class FileApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private String redisPort;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }
}
