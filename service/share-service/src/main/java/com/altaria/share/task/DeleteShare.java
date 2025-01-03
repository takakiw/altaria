package com.altaria.share.task;


import com.altaria.common.pojos.share.entity.Share;
import com.altaria.share.cache.ShareCacheService;
import com.altaria.share.mapper.ShareMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class DeleteShare {

    @Autowired
    private ShareMapper shareMapper;

    @Autowired
    private ShareCacheService shareCacheService;

    @Scheduled(cron = "0 0 * * * *") // 每小时执行一次
    public void deleteExpiredShare() {
        // 查询过期分享
        List<Share> expiredShare = shareMapper.getExpiredShare();
        // 数据库删除过期分享
        shareMapper.deleteByExpire();
        // 缓存删除过期分享
        shareCacheService.deleteShareBatch(expiredShare);
        log.info("删除过期分享{}", LocalDateTime.now());
    }
}
