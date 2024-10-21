package com.altaria.feign.config;

import com.altaria.common.constants.FeignConstants;
import com.altaria.feign.fallback.FileServiceClientFallbackFactory;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileServiceFeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public FileServiceClientFallbackFactory fileServiceClientFallbackFactory() {
        return new FileServiceClientFallbackFactory();
    }


    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header(FeignConstants.REQUEST_ID_HEADER, FeignConstants.REQUEST_ID_VALUE);
            template.header("signature", "gateway");
        };
    }
}
