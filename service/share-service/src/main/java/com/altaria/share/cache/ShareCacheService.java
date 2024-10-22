package com.altaria.share.cache;

import com.altaria.common.pojos.share.entity.Share;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ShareCacheService {

    private static final String SHARE_PREFIX = "share:";
    private static final long SHARE_EXPIRE_TIME = 60 * 60 * 24 * 2; // 2 days
    private static final long NULL_SHARE_EXPIRE_TIME = 60 * 5;
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
}
