package com.altaria.share.cache;

import com.altaria.common.pojos.share.entity.Share;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ShareCacheService {

    private static final String SHARE_PREFIX = "share:";
    private static final String USER_SHARE_PREFIX = "user-share:";
    private static final long SHARE_EXPIRE_TIME = 60 * 60 * 24 * 2; // 2 days
    private static final long NULL_SHARE_EXPIRE_TIME = 60 * 5;

    private static final long USER_SHARE_EXPIRE_TIME = 60 * 60 * 24 * 2; // 7 days
    private static final long NULL_USER_SHARE_EXPIRE_TIME = 2 * 60 * 60; // 2 hours

    private static final long NULL_USER_SHARE_VALUE = 1000000000000000000L;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Share getShareInfo(Long shareId) {
        if (shareId == NULL_USER_SHARE_VALUE){
            return null;
        }
        return (Share) redisTemplate.opsForValue().get(SHARE_PREFIX + shareId);
    }

    @Async
    public void saveNullShareInfo(Long shareId) {
        Share share = new Share();
        share.setName("null");
        redisTemplate.opsForValue().set(SHARE_PREFIX + shareId, share, NULL_SHARE_EXPIRE_TIME, TimeUnit.SECONDS);
    }


    @Async
    public void saveShareInfo(Share shareInfo) {
        redisTemplate.opsForValue().set(SHARE_PREFIX + shareInfo.getId(), shareInfo, SHARE_EXPIRE_TIME, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(USER_SHARE_PREFIX + shareInfo.getUid()))){
            redisTemplate.opsForZSet().add(USER_SHARE_PREFIX + shareInfo.getUid(), shareInfo.getId(), shareInfo.getCreateTime().toEpochSecond(ZoneOffset.UTC));
        }
    }


    public List<Share> getUserAllShare(Long userId) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(USER_SHARE_PREFIX + userId))){
            return null;
        }
        Set<Object> range = redisTemplate.opsForZSet().range(USER_SHARE_PREFIX + userId, 0, -1);
        if (range != null) {
            range.remove(NULL_USER_SHARE_VALUE);
        }
        if (range != null && range.size() > 0) {
            return range.stream().map(shareId -> getShareInfo((Long) shareId)).toList();
        }
        return null;
    }

    @Async
    public void saveUserAllShare(Long userId, List<Share> shareList) {
        redisTemplate.delete(USER_SHARE_PREFIX + userId);
        shareList.forEach(share -> {
            redisTemplate.opsForZSet().add(USER_SHARE_PREFIX + userId, share.getId(), share.getCreateTime().toEpochSecond(ZoneOffset.UTC)); // 保存子键
            redisTemplate.opsForValue().set(SHARE_PREFIX + share.getId(), share, SHARE_EXPIRE_TIME, TimeUnit.SECONDS); // 设置值
        });
        redisTemplate.expire(USER_SHARE_PREFIX + userId, USER_SHARE_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public Boolean KeyExists(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(USER_SHARE_PREFIX + userId));
    }

    @Async
    public void deleteShareBatch(List<Share> shareList) {
        shareList.forEach(share -> {
            redisTemplate.delete(SHARE_PREFIX + share.getId());
            if (Boolean.TRUE.equals(redisTemplate.hasKey(USER_SHARE_PREFIX + share.getUid()))){
                redisTemplate.opsForZSet().remove(USER_SHARE_PREFIX + share.getUid(), share.getId());
            }
        });
    }


    @Async
    public void saveUserNullChild(Long userId) {
        redisTemplate.opsForZSet().add(USER_SHARE_PREFIX + userId, NULL_USER_SHARE_VALUE, System.currentTimeMillis());
        redisTemplate.expire(USER_SHARE_PREFIX + userId, NULL_USER_SHARE_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    @Async
    public void incrementVisit(Long shareId) {
        Share share = getShareInfo(shareId);
        if (share != null) {
            share.setVisit(share.getVisit() + 1);
            redisTemplate.opsForValue().set(SHARE_PREFIX + shareId, share, SHARE_EXPIRE_TIME, TimeUnit.SECONDS);
        }
    }
}
