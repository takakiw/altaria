package com.altaria.file.task;

import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.file.cache.FileCacheService;
import com.altaria.file.mapper.FileInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RecycleDelTask {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private FileCacheService fileCacheService;


    @Scheduled(cron = "0 0 * * * ?")
    public void deleteRecycleFiles(){
        // 判断更新时间在30天前的文件是否已经被删除
        // 若未被删除，则删除
        LocalDateTime expiredTime = LocalDateTime.now().minusDays(30);
        fileInfoMapper.deleteExpiredRecycleFiles(expiredTime);
    }

}
