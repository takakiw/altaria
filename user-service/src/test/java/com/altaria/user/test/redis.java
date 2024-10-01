package com.altaria.user.test;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.utils.JWTUtil;
import com.altaria.redis.UserRedisService;
import com.altaria.user.mapper.UserMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;

@SpringBootTest
public class redis {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRedisService userRedisService;

    @Autowired
    private UserMapper userMapper;

    @Test
    public void test() {
        redisTemplate.keys("*").forEach(System.out::println);
        redisTemplate.opsForValue().set("test", "test");
        System.out.println(redisTemplate.opsForValue().get("test"));
        userRedisService.saveEmailCode(UserConstants.TYPE_LOGIN, RandomStringUtils.random(6, true, true), "2918628219@test.com");
        System.out.println(userRedisService.getEmailCode(UserConstants.TYPE_LOGIN, "2918628219@test.com"));
    }

    @Test
    public void test2() {
        System.out.println(DigestUtils.md5DigestAsHex("qq123321".getBytes()));
    }

    @Test
    public void test3() {
        User user = userMapper.getUserById(1L);
        String jwt = JWTUtil.userToJWT(user);
        System.out.println(jwt);
        System.out.println(JWTUtil.parseJwt(jwt));
    }
}
