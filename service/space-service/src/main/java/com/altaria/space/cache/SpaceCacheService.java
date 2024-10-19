package com.altaria.space.cache;


import com.altaria.common.pojos.space.entity.Space;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SpaceCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long SPACE_EXPIRATION_TIME = 60 * 60 * 24 * 2L; // 2 days
    private static final String SPACE_PREFIX = "space:";


    public void saveSpace(Space space) {
        redisTemplate.opsForValue().set(SPACE_PREFIX + space.getUid(), space, SPACE_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    public Space getSpace(Long uid) {
        return (Space) redisTemplate.opsForValue().get(SPACE_PREFIX + uid);
    }

    public void deleteSpace(Long uid) {
        redisTemplate.delete(SPACE_PREFIX + uid);
    }
}
