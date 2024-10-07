package com.altaria.feign.fallback;

import com.altaria.common.pojos.common.Result;
import com.altaria.feign.client.UserServiceClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {
    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {

            @Override
            public Result getShareUserById(Long uId) {
                return Result.error("User Service is not available");
            }
        };
    }
}
