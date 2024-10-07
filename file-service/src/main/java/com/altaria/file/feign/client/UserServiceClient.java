package com.altaria.file.feign.client;


import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
import com.altaria.file.feign.fallback.UserServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(contextId = "userServiceClient", name = "gateway-service", fallbackFactory = UserServiceClientFallbackFactory.class)
public interface UserServiceClient {

    @GetMapping("/user/user/space")
    public Result getShareUserById(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uId);

}
