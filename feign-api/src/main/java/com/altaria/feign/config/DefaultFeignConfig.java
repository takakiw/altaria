package com.altaria.feign.config;

import com.altaria.feign.fallback.FileServiceClientFallbackFactory;
import com.altaria.feign.fallback.SpaceServiceClientFallbackFactory;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public FileServiceClientFallbackFactory userServiceClientFallbackFactory() {
        return new FileServiceClientFallbackFactory();
    }

    @Bean
    public SpaceServiceClientFallbackFactory spaceServiceClientFallbackFactory() {
        return new SpaceServiceClientFallbackFactory();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("request-path-service", "feign-api");
        };
    }
}
