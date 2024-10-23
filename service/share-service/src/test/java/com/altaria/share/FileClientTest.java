package com.altaria.share;

import com.altaria.feign.client.FileServiceClient;
import com.altaria.share.cache.ShareCacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Arrays;

@SpringBootTest
public class FileClientTest {

    @Autowired
    private FileServiceClient fileServiceClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ShareCacheService cacheService;
    @Test
    public void test() {
        System.out.println(fileServiceClient.getPath(1L, 1L));
        System.out.println(fileServiceClient.getFileInfos(Arrays.asList(1L, 2L), 1L));
    }

    @Test
    public void testd1() {
        System.out.println(cacheService.KeyExists(5L));
        redisTemplate.opsForZSet().range("user-share:5", 0, -1).forEach(System.out::println);
        ArrayList<Long> objects = new ArrayList<>();
        objects.add(1848391050913714176L);
        objects.add(1849000414477357056L);
        System.out.println("删除前");
        redisTemplate.opsForZSet().remove("user-share:5", objects.toArray());
        redisTemplate.opsForZSet().range("user-share:5", 0, -1).forEach(System.out::println);
    }

}
