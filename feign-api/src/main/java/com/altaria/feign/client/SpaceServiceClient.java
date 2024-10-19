package com.altaria.feign.client;


import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.space.entity.Space;
import com.altaria.common.pojos.space.vo.SpaceVO;
import com.altaria.feign.fallback.SpaceServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(contextId = "spaceServiceClient", value = "gateway-service", fallbackFactory = SpaceServiceClientFallbackFactory.class, path = "/space")
public interface SpaceServiceClient {

    @GetMapping("/info")
    public Result<SpaceVO> space(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid);

    @PutMapping("/update")
    public Result updateSpace(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                              @RequestBody Space space);
}
