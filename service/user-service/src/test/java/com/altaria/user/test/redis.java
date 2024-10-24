package com.altaria.user.test;

import com.altaria.common.constants.UserConstants;

import com.altaria.user.cache.UserCacheService;
import org.apache.commons.lang3.RandomStringUtils;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootTest
public class redis {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Autowired
    private UserCacheService userRedisService;



    @Test
    public void test() {
        Long l = null;
        try {
            l = System.currentTimeMillis();
            System.out.println(userRedisService.getUserById(1L));
            System.out.println(System.currentTimeMillis() - l);

            l = System.currentTimeMillis();
            System.out.println(Boolean.TRUE.equals(redisTemplate.getConnectionFactory().getConnection().ping()));

        }catch (Exception e){
            System.out.println(System.currentTimeMillis() - l);
            System.out.println("redis连接失败");
        }
    }
}
