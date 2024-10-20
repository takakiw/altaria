package com.altaria.share.task;


import com.altaria.share.mapper.ShareMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class DeleteShare {

    @Autowired
    private ShareMapper shareMapper;

    @Scheduled(cron = "0 0 * * * *") // 每小时执行一次
    public void deleteExpiredShare() {
        shareMapper.deleteByExpire();
        log.info("删除过期分享{}", LocalDateTime.now());
    }
}
