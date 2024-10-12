package com.altaria.file;


import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.file.cache.FileCacheService;
import com.altaria.file.mapper.FileInfoMapper;
import com.github.pagehelper.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class CacheTest {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private FileCacheService cacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testCache() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setUid(1L);
        Page<FileInfo> select = fileInfoMapper.select(fileInfo);
        List<FileInfo> result = select.getResult();
        cacheService.saveAllChildren(1L, 0L, result);


        /*redisTemplate.opsForZSet().add("test", "a", 1);
        redisTemplate.opsForZSet().add("test", "b", 2);
        redisTemplate.opsForZSet().add("test", "c", 3);
        redisTemplate.opsForZSet().range("test", 0, -1).forEach(System.out::println);*/

        List<Long> longs = redisTemplate.opsForZSet().range("parent:update1:0", 0, -1).stream().map(o -> (Long) o).toList();
        System.out.println(longs);
        List<Long> longs1 = redisTemplate.opsForZSet().range("parent:update11:0", 0, -1).stream().map(o -> (Long) o).toList();
        System.out.println(longs1.isEmpty());


        List<Long> longs2 = redisTemplate.opsForZSet().range("parent:size1:0", 0, -1).stream().map(o -> (Long) o).toList();
        System.out.println(longs2);
        List<Long> longs3 = redisTemplate.opsForZSet().range("parent:size11:0", 0, -1).stream().map(o -> (Long) o).toList();
        System.out.println(longs3.isEmpty());

        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().rangeWithScores("parent:name1:0", 0, -1);
        for (ZSetOperations.TypedTuple<Object> typedTuple : typedTuples){
            System.out.println(typedTuple.getValue() + " " + typedTuple.getScore());
        }
    }


    @Test
    public void testCache1() {
        cacheService.deleteFile(1L, 1L);
        cacheService.deleteFileBatch(1L, List.of(1L, 2L, 3L));
        cacheService.deleteChildren(1L, 0L, 1L);
        cacheService.deleteAllChildren(1L, 0L);
        cacheService.deleteChildrenBatch(new ArrayList<>());

        /*cacheService.updateFileParent(1L, 1L, 2L);
        cacheService.updateFileName(1L, 1L, "test");
        cacheService.updateFileSize(1L, 1L, 100L);*/


        redisTemplate.opsForHash().delete("file:1:1", "filaaeName");
    }


}
