package com.altaria.redis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CheckConnectTask {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TaskScheduler taskScheduler;

    private boolean isRedisConnected = false;

    @PostConstruct
    public void init() {
        scheduleRedisCheck();
    }
    public void scheduleRedisCheck() {
        taskScheduler.scheduleWithFixedDelay(this::checkRedisConnection, 10000);
    }

    public void checkRedisConnection() {
        try {
            if ("PONG".equals(redisTemplate.getConnectionFactory().getConnection().ping())) {
                isRedisConnected = true;
            }
        } catch (Exception e) {
            isRedisConnected = false;
            log.warn("Redis connection check failed");
        }
    }

    public boolean isRedisConnected() {
        return isRedisConnected;
    }

    public void setRedisConnected(boolean redisConnected) {
        isRedisConnected = redisConnected;
    }
}
