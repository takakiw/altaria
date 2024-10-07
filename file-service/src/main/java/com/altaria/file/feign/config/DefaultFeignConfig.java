package com.altaria.file.feign.config;

import com.altaria.file.feign.fallback.UserServiceClientFallbackFactory;
import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultFeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public UserServiceClientFallbackFactory userServiceClientFallbackFactory() {
        return new UserServiceClientFallbackFactory();
    }
}
