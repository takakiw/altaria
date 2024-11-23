package com.altaria.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {
    private String host;
    private int port;
    private int database;
}