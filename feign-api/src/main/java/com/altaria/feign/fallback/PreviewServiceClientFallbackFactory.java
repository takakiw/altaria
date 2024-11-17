package com.altaria.feign.fallback;

import com.altaria.common.pojos.common.Result;
import com.altaria.feign.client.PreviewServiceClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class PreviewServiceClientFallbackFactory implements FallbackFactory<PreviewServiceClient> {
    @Override
    public PreviewServiceClient create(Throwable cause) {
        return new PreviewServiceClient() {
            @Override
            public Result<String> sign(Long id, Long uid, String category) {
                return Result.error();
            }
        };
    }
}
