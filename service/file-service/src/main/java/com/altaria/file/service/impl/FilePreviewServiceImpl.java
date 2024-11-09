package com.altaria.file.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.altaria.common.constants.FileConstants;
import com.altaria.common.enums.FileType;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.common.utils.SignUtil;
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

    private static final Long DEFAULT_LENGTH = 1024 * 512L; // 默认预览文件大小512KB

    @Autowired
    private MinioService minioService;



    @Override
    public void preview(HttpServletResponse response, Long id, Long uid, Long expire, String sign) {
        if (id == null || uid == null) {
            writerResponse(response, StatusCodeEnum.PARAM_NOT_NULL);
            return;
        }
        boolean checkSign = SignUtil.checkSign(uid, id, expire, sign);
        if (!checkSign){
            writerResponse(response, StatusCodeEnum.PARAM_ERROR);
            return;
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
            writerResponse(response, StatusCodeEnum.ILLEGAL_REQUEST); // 文件不能预览
            return;
        }
        if (file.getUrl() == null || file.getTransformed() != FileConstants.TRANSFORMED_END){
            log.info("preview file id: {}", id);
            writerResponse(response, StatusCodeEnum.FILE_TRANSCODING);
            return;
        }
        minioService.preview(file.getUrl(), response);
    }

    @Override
    public void previewCover(HttpServletResponse response, Long id, Long uid, Long expire, String sign) {
        if (id == null || uid == null){
            writerResponse(response, StatusCodeEnum.PARAM_NOT_NULL);
            return;
        }
        boolean checkSign = SignUtil.checkSign(uid, id, expire, sign);
        if (!checkSign){
            writerResponse(response, StatusCodeEnum.PARAM_ERROR);
            return;
        }
        FileInfo file = cacheService.getFile(uid, id);
        if (file == null){
            file = fileInfoMapper.getFileById(id, uid);
            if (file == null) file = fileInfoMapper.getRecycleFile(uid, id);
            if (file == null){
                cacheService.saveNullFile(uid, id);
                writerResponse(response, StatusCodeEnum.FILE_NOT_EXISTS);
                return;
            }
            if (file.getStatus().compareTo(FileConstants.STATUS_USE) == 0) cacheService.saveFile(file);
        }
        if (file.getId() != null &&
                (file.getType().compareTo(FileType.VIDEO.getType()) == 0 || file.getType().compareTo(FileType.IMAGE.getType()) == 0)){
            if (file.getCover() == null || file.getTransformed() != FileConstants.TRANSFORMED_END){
                log.info("preview cover id: {}", id);
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
    public Result<String> sign(Long id, Long uid, String category) {
        if (id == null || uid == null){
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        if (category == null || !category.equals("cover")){
            FileInfo file = cacheService.getFile(uid, id);
            if (file == null){
                file = fileInfoMapper.getFileById(id, uid);
                if (file == null){
                    cacheService.saveNullFile(uid, id);
                    return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
                }
                cacheService.saveFile(file);
            }
            if (file.getId() == null || file.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
                return Result.error(StatusCodeEnum.FILE_CANNOT_PREVIEW);
            }
            if (file.getUrl() == null || file.getTransformed() != FileConstants.TRANSFORMED_END){
                log.info("sign file id: {}", id);
                return Result.error(StatusCodeEnum.FILE_TRANSCODING);
            }
            // expires 过期时间，时间戳+8h
            long expires = System.currentTimeMillis() + 8 * 60 * 60 * 1000;
            String sign = SignUtil.sign(uid, id, expires);
            String signUrl = "";
            if (file.getType().compareTo(FileType.VIDEO.getType()) == 0 || file.getType().compareTo(FileType.AUDIO.getType()) == 0){
                signUrl = "/file/preview/video/";
            } else {
                signUrl = "/file/preview/file/";
            }
            signUrl += id + "?" + "expire=" + expires + "&uid=" + uid + "&sign=" + sign;
            return Result.success(signUrl);
        }else { // 预览封面
            FileInfo file = cacheService.getFile(uid, id);
            if (file == null){
                file = fileInfoMapper.getFileById(id, uid);
                if (file == null) file = fileInfoMapper.getRecycleFile(uid, id);
                if (file == null){
                    cacheService.saveNullFile(uid, id);
                    return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
                }
                if (file.getStatus().compareTo(FileConstants.STATUS_USE) == 0) cacheService.saveFile(file);
            }
            if (file.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
                return Result.error(StatusCodeEnum.FILE_CANNOT_PREVIEW);
            }
            if (file.getCover() == null || file.getTransformed() != FileConstants.TRANSFORMED_END){
                log.info("sign cover id: {}", id);
                return Result.error(StatusCodeEnum.FILE_TRANSCODING);
            }
            // expires 过期时间，时间戳+8h
            long expires = System.currentTimeMillis() + 8 * 60 * 60 * 1000;
            String sign = SignUtil.sign(uid, id, expires);
            String signUrl = "/file/preview/cover/";
            signUrl += id + "?" + "expire=" + expires + "&uid=" + uid + "&sign=" + sign;
            return Result.success(signUrl);
        }
    }


    @Override
    public void previewVideo(HttpServletRequest request, HttpServletResponse response, Long id, Long uid, Long expire, String sign) {
        if (id == null || uid == null || expire == null || sign == null) {
            log.info("参数错误");
            writerResponse(response, StatusCodeEnum.PARAM_NOT_NULL);
            return;
        }

        boolean checkSign = SignUtil.checkSign(uid, id, expire, sign);
        if (!checkSign){
            writerResponse(response, StatusCodeEnum.PARAM_ERROR);
            return;
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

        if (file.getId() == null){
            writerResponse(response, StatusCodeEnum.FILE_CANNOT_PREVIEW);
            return;
        }
        // 视频文件才可以预览
        if (file.getType().compareTo(FileType.VIDEO.getType()) != 0
        && file.getType().compareTo(FileType.AUDIO.getType()) != 0){
            writerResponse(response, StatusCodeEnum.FILE_CANNOT_PREVIEW);
            return;
        }
        if (file.getUrl() == null || file.getTransformed() != FileConstants.TRANSFORMED_END){
            log.info("preview video id: {}", id);
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
