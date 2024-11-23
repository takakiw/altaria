package com.altaria.file.task;

import com.altaria.common.constants.FileConstants;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.file.mapper.FileInfoMapper;
import com.altaria.file.service.FileManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecycleDelTask {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private FileManagementService fileManagementService;


    @Scheduled(cron = "0 0 * * * ?")
    public void deleteRecycleFiles(){
        // 判断更新时间在30天前的文件是否已经被删除
        // 若未被删除，则删除
        LocalDateTime expiredTime = LocalDateTime.now().minusSeconds(FileConstants.RECYCLE_EXPIRE_TIME); // 回收站过期时间前
        System.out.println(expiredTime);
        List<FileInfo> fileInfos = fileInfoMapper.getAllRecycleFiles(expiredTime);
        if (fileInfos!= null && fileInfos.size() > 0){
            fileManagementService.removeRecycleFile(fileInfos.stream().map(FileInfo::getId).toList());
        }
    }

}
