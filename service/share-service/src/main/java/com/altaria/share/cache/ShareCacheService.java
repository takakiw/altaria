package com.altaria.share.cache;

import com.altaria.common.pojos.share.entity.Share;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ShareCacheService {

    private static final String SHARE_PREFIX = "share:";
    private static final String USER_SHARE_PREFIX = "user-share:";
    private static final long SHARE_EXPIRE_TIME = 60 * 60 * 24 * 2; // 2 days
    private static final long NULL_SHARE_EXPIRE_TIME = 60 * 5;

    private static final long USER_SHARE_EXPIRE_TIME = 60 * 60 * 24 * 2; // 7 days
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Share getShareInfo(Long shareId) {
        return (Share) redisTemplate.opsForValue().get(SHARE_PREFIX + shareId);
    }

    public void saveNullShareInfo(Long shareId) {
        Share share = new Share();
        share.setName("null");
        redisTemplate.opsForValue().set(SHARE_PREFIX + shareId, share, NULL_SHARE_EXPIRE_TIME, TimeUnit.SECONDS);
    }


    public void saveShareInfo(Share shareInfo) {
        redisTemplate.opsForValue().set(SHARE_PREFIX + shareInfo.getId(), shareInfo, SHARE_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void saveShareBatch(List<Share> shareList) {
        // 批量保存
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();

        // 使用 Redis Pipeline
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Share share : shareList) {
                String key = SHARE_PREFIX + share.getId();
                valueOps.set(key, share); // 设置值
                // 这里是直接使用连接入口来设置过期时间
                connection.expire(key.getBytes(), SHARE_EXPIRE_TIME); // 设置过期时间
            }
            return null; // executePipelined不需要返回结果
        });
    }

    public List<Share> getUserAllShare(Long userId) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(USER_SHARE_PREFIX + userId))){
            return null;
        }
        return redisTemplate.opsForZSet().range(USER_SHARE_PREFIX + userId, 0, -1).stream().map(shareId -> getShareInfo((Long) shareId)).toList();
    }

    public void saveUserAllShare(Long userId, List<Share> shareList) {
        redisTemplate.delete(USER_SHARE_PREFIX + userId);
        shareList.forEach(share -> redisTemplate.opsForZSet().add(USER_SHARE_PREFIX + userId, share.getId(), share.getCreateTime().toEpochSecond(ZoneOffset.UTC)));
        redisTemplate.expire(USER_SHARE_PREFIX + userId, USER_SHARE_EXPIRE_TIME, TimeUnit.SECONDS);
        saveShareBatch(shareList);
    }

    public boolean KeyExists(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(USER_SHARE_PREFIX + userId));
    }

    public void deleteShareBatch(List<Share> shareList) {
        redisTemplate.delete(shareList.stream().map(share -> SHARE_PREFIX + share.getId()).collect(Collectors.toList()));
    }

    public void deleteUserShare(Long userId, List<Long> realIds) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(USER_SHARE_PREFIX + userId))){
            redisTemplate.opsForZSet().remove(USER_SHARE_PREFIX + userId, realIds);
        }
    }
}
