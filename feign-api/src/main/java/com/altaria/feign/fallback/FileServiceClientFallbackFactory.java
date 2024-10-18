package com.altaria.feign.fallback;

import com.altaria.feign.client.FileServiceClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


@Component
public class FileServiceClientFallbackFactory implements FallbackFactory<FileServiceClient> {
    @Override
    public FileServiceClient create(Throwable cause) {
        return new FileServiceClient() {
            @Override
            public String uploadImage(MultipartFile file) {
                return null;
            }
        };
    }
}
