package com.altaria.minio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    private String endpoint;

    private String bucketName;

    private String accessKey;

    private String secretKey;
}
