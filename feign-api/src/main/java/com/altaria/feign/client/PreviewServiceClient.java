package com.altaria.feign.client;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
import com.altaria.feign.fallback.PreviewServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(contextId = "previewServiceClient", value = "preview-service", fallbackFactory = PreviewServiceClientFallbackFactory.class, path = "/preview")
public interface PreviewServiceClient {
    @GetMapping("/sign/{id}")
    Result<String> sign(@PathVariable("id") Long id,
                               @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                               @RequestParam(value = "category", defaultValue = "file", required = false) String category);
}
