package com.altaria.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserRedisService {

    private static final String REDIS_KEY_PREFIX = "user:";
    private static final Long USER_EXPIRATION_TIME = 60 * 60 * 24 * 7L; // 7 days

    private static final String EMAIL_CODE_KEY = "email_code:";
    private static final Long CODE_EXPIRATION_TIME = 60 * 2L; // 2 minutes

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveCode(String code, String email) {
        redisTemplate.opsForValue().set(EMAIL_CODE_KEY + email, code, CODE_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public String getCode(String email) {
        return (String) redisTemplate.opsForValue().get(EMAIL_CODE_KEY + email);
    }

}
