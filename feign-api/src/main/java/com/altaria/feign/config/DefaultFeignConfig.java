package com.altaria.feign.config;

import com.altaria.feign.fallback.UserServiceClientFallbackFactory;
import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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
