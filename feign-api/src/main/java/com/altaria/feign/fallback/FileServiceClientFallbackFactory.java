package com.altaria.feign.fallback;

import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.common.pojos.file.entity.SaveShare;
import com.altaria.feign.client.FileServiceClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Component
public class FileServiceClientFallbackFactory implements FallbackFactory<FileServiceClient> {
    @Override
    public FileServiceClient create(Throwable cause) {
        return new FileServiceClient() {
            @Override
            public String uploadImage(MultipartFile file) {
                return null;
            }

            @Override
            public Result<List<FileInfo>> getFileInfos(List<Long> fids, Long uid) {
                return Result.error();
            }

            @Override
            public Result<List<FileInfo>> getPath(Long path, Long uid) {
                return Result.error();
            }

            @Override
            public Result<PageResult<FileInfo>> getChildrenList(Long id, Integer type, String fileName, Long uid, Integer order) {
                return Result.error();
            }

            @Override
            public Result saveFileToCloud(SaveShare saveShare) {
                return Result.error();
            }

            @Override
            public Result<String> downloadSign(Long id, Long uid) {
                return Result.error();
            }

            @Override
            public Result<FileInfo> getFileInfo(Long id, Long uid) {
                return Result.error();
            }
        };
    }
}
