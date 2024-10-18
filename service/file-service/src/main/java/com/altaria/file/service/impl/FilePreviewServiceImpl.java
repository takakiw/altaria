package com.altaria.file.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.altaria.common.enums.FileType;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.file.cache.FileCacheService;
import com.altaria.file.mapper.FileInfoMapper;
import com.altaria.file.service.FilePreviewService;
import com.altaria.minio.service.MinioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class FilePreviewServiceImpl implements FilePreviewService {

    @Autowired
    private FileCacheService cacheService;

    @Autowired
    private FileInfoMapper fileInfoMapper;

    private static final Long DEFAULT_LENGTH = 1024 * 1024 * 2L;

    @Autowired
    private MinioService minioService;



    @Override
    public void preview(HttpServletResponse response, Long id, Long uid) {
        if (id == null || uid == null) {
            writerResponse(response, StatusCodeEnum.PARAM_NOT_NULL);
        }
        FileInfo file = cacheService.getFile(uid, id);
        if (file == null){
            file = fileInfoMapper.getFileById(id, uid);
            if (file == null){
                cacheService.saveNullFile(uid, id);
                writerResponse(response, StatusCodeEnum.FILE_NOT_EXISTS);
                return;
            }
            cacheService.saveFile(file);
        }
        if (file.getId() == null
                || file.getType().equals(FileType.DIRECTORY.getType())
                || file.getType().equals(FileType.VIDEO.getType())) {
            writerResponse(response, StatusCodeEnum.FILE_CANNOT_PREVIEW); // 文件不能预览
            return;
        }
        if (file.getUrl() == null){
            writerResponse(response, StatusCodeEnum.FILE_TRANSCODING);
            return;
        }
    }

    @Override
    public void previewCover(HttpServletResponse response, Long id, Long uid) {
        if (id == null || uid == null){
            writerResponse(response, StatusCodeEnum.PARAM_NOT_NULL);
        }
        FileInfo file = cacheService.getFile(uid, id);
        if (file == null){
            file = fileInfoMapper.getFileById(id, uid);
            if (file == null){
                cacheService.saveNullFile(uid, id);
                writerResponse(response, StatusCodeEnum.FILE_NOT_EXISTS);
                return;
            }
            cacheService.saveFile(file);
        }
        if (file.getId() != null &&
                (file.getType().compareTo(FileType.VIDEO.getType()) == 0 || file.getType().compareTo(FileType.IMAGE.getType()) == 0)){
            if (file.getCover() == null){
                writerResponse(response, StatusCodeEnum.FILE_TRANSCODING);
            }else {
                minioService.preview(file.getCover(), response);
            }
            return;
        }
        writerResponse(response, StatusCodeEnum.FILE_CANNOT_PREVIEW);
        return;
    }

    @Override
    public void previewVideo(HttpServletRequest request, HttpServletResponse response, Long id, Long uid) {
        if (id == null || uid == null) {
            writerResponse(response, StatusCodeEnum.PARAM_NOT_NULL);
        }
        FileInfo file = cacheService.getFile(uid, id);
        if (file == null){
            file = fileInfoMapper.getFileById(id, uid);
            if (file == null){
                cacheService.saveNullFile(uid, id);
                writerResponse(response, StatusCodeEnum.FILE_NOT_EXISTS);
                return;
            }
            cacheService.saveFile(file);
        }
        if (file.getId() == null ||
                !file.getType().equals(FileType.VIDEO.getType())){
            writerResponse(response, StatusCodeEnum.FILE_CANNOT_PREVIEW); // 文件不能预览
            return;
        }
        if (file.getUrl() == null){
            writerResponse(response, StatusCodeEnum.FILE_TRANSCODING);
            return;
        }
        String range = request.getHeader("Range");
        range = range == null ? "bytes=0-" : range;
        String[] split = range.replace("bytes=", "").split("-");
        long start = Long.parseLong(split[0]);
        long end = split.length > 1 ? Long.parseLong(split[1]) : start + DEFAULT_LENGTH - 1;
        minioService.previewVideo(file.getUrl(), response, start, end);
    }

    public void writerResponse(HttpServletResponse response, StatusCodeEnum statusCode){
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(JSONObject.toJSONString(Result.error(statusCode)));
        } catch (IOException e) {
            log.error("请求头设置失败", e);
        }
    }
}
