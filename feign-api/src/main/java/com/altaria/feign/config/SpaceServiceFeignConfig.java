package com.altaria.feign.config;

import com.altaria.common.constants.FeignConstants;
import com.altaria.feign.fallback.SpaceServiceClientFallbackFactory;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class SpaceServiceFeignConfig {


    @Bean
    public SpaceServiceClientFallbackFactory spaceServiceClientFallbackFactory() {
        return new SpaceServiceClientFallbackFactory();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header(FeignConstants.REQUEST_ID_HEADER, FeignConstants.REQUEST_ID_VALUE);
            template.header("signature", "gateway");
        };
    }
}
