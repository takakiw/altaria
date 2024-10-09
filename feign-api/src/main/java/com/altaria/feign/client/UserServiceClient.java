package com.altaria.feign.client;


import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
import com.altaria.feign.fallback.UserServiceClientFallbackFactory;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(contextId = "UserServiceClient", name = "gateway-service", fallbackFactory = UserServiceClientFallbackFactory.class)
public interface UserServiceClient {


}
