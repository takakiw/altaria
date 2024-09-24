package com.altaria.user.test;

import com.altaria.common.redis.UserRedisService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class redis {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRedisService userRedisService;

    @Test
    public void test() {
        redisTemplate.keys("*").forEach(System.out::println);
        redisTemplate.opsForValue().set("test", "test");
        System.out.println(redisTemplate.opsForValue().get("test"));
        userRedisService.saveCode(RandomStringUtils.random(6, true, true), "2918628219@test.com");
        System.out.println(userRedisService.getCode("2918628219@test.com"));
    }

}
