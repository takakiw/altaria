package com.altaria.minio.config;

import com.altaria.minio.service.MinioService;
import com.altaria.minio.service.impl.MinioServiceImpl;
import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {

    private String endpoint;

    private String bucketName;

    private String accessKey;

    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public MinioService minioService(MinioClient minioClient) {
        return new MinioServiceImpl();
    }

}
