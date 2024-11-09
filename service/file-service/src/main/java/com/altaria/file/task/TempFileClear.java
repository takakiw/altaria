package com.altaria.file.task;

import com.altaria.file.cache.FileCacheService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class TempFileClear {

    @Value("${temp.file.path}")
    private String tempFilePath;

    @Autowired
    private FileCacheService fileCacheService;

    @Scheduled(cron = "0 0 * * * ?") // 每天凌晨执行
    public void clearTempFiles() throws IOException {
        // 查询过期的文件，并删除
        File sourceDir = new File(tempFilePath);
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files){
                try{
                    Long.parseLong(file.getName());
                }catch (NumberFormatException e){
                    continue;
                }
                boolean b = fileCacheService.existsUploadFile(Long.parseLong(file.getName()));
                if (!b){
                    FileUtils.deleteDirectory(file);
                }
            }
        }
    }
}
