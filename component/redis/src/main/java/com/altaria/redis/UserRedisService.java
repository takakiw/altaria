package com.altaria.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserRedisService {

    private static final Long USER_EXPIRATION_TIME = 60 * 60 * 24 * 7L; // 7 days

    private static final Long CODE_EXPIRATION_TIME = 60 * 2L; // 2 minutes


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存用户验证码到redis
     * @param code
     * @param email
     */
    public void saveEmailCode(String type, String code, String email) {
        redisTemplate.opsForValue().set(type + ":" + email, code, CODE_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    /**
     * 获取用户验证码
     * @param email
     * @return
     */
    public String getEmailCode(String type, String email) {
        Object o = redisTemplate.opsForValue().get(type + ":" + email);
        if (o != null) {
            return o.toString();
        }
        return null;
    }

    public int getEmailCodeTTL(String type, String email) {
        Long expire = redisTemplate.getExpire(type + ":" + email, TimeUnit.SECONDS);
        if (expire == null) {
            return 0;
        }
        return expire.intValue();
    }
}
