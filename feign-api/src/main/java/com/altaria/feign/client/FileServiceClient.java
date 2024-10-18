package com.altaria.feign.client;

import com.altaria.feign.fallback.FileServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(contextId = "fileServiceClient", value = "gateway-service", fallbackFactory = FileServiceClientFallbackFactory.class, path = "/file")
public interface FileServiceClient {

    @RequestMapping(value = "/upload-image",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,method = {RequestMethod.POST},produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},headers = "content-type=multipart/form-data")
    public String uploadImage(@RequestPart("file") MultipartFile file);
}
