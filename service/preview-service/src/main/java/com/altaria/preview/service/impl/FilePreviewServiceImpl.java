package com.altaria.preview.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.altaria.common.constants.FileConstants;
import com.altaria.common.enums.FileType;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.common.utils.SignUtil;
import com.altaria.feign.client.FileServiceClient;
import com.altaria.minio.service.MinioService;
import com.altaria.preview.service.FilePreviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class FilePreviewServiceImpl implements FilePreviewService {


    private static final Long DEFAULT_LENGTH = 1024 * 512L; // 默认预览文件大小512KB

    @Autowired
    private MinioService minioService;

    @Autowired
    private FileServiceClient fileServiceClient;

    @Value("${api.file}")
    private String apiFile;

    @Value("${api.video}")
    private String apiVideo;

    @Value("${api.cover}")
    private String apiCover;


    @Override
    public void preview(HttpServletResponse response, String url, Long uid, Long expire, String sign) {
        boolean checkSign = SignUtil.checkSign(uid, url, expire, sign);
        if (!checkSign){
            writerResponse(response, StatusCodeEnum.PARAM_ERROR);
            return;
        }
        minioService.preview(url, response);
    }

    @Override
    public void previewCover(HttpServletResponse response, String url, Long uid, Long expire, String sign) {
        boolean checkSign = SignUtil.checkSign(uid, url, expire, sign);
        if (!checkSign){
            writerResponse(response, StatusCodeEnum.PARAM_ERROR);
            return;
        }
        minioService.preview(url, response);
    }

    @Override
    public Result<String> sign(Long id, Long uid, String category) {
        if (id == null || uid == null){
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        if (category == null || !category.equals("cover")){
            Result<FileInfo> fileInfo = fileServiceClient.getFileInfo(id, uid);
            if (fileInfo.getCode() != StatusCodeEnum.SUCCESS.getCode()){
                return Result.error(StatusCodeEnum.ERROR);
            }
            if (fileInfo.getData() == null){
                return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
            }
            FileInfo file = fileInfo.getData();
            if (file.getId() == null || file.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
                return Result.error(StatusCodeEnum.FILE_CANNOT_PREVIEW);
            }
            if (file.getUrl() == null || file.getTransformed() != FileConstants.TRANSFORMED_END){
                log.info("sign file id: {}", id);
                return Result.error(StatusCodeEnum.FILE_TRANSCODING);
            }
            // expires 过期时间，时间戳+8h
            long expires = System.currentTimeMillis() + 8 * 60 * 60 * 1000;
            String fileUrl = file.getUrl();
            String sign = SignUtil.sign(uid, fileUrl, expires);
            String signUrl = "";
            if (file.getType().compareTo(FileType.VIDEO.getType()) == 0 || file.getType().compareTo(FileType.AUDIO.getType()) == 0){
                signUrl = apiVideo;
            } else {
                signUrl = apiFile;
            }
            signUrl += fileUrl + "?" + "expire=" + expires + "&uid=" + uid + "&sign=" + sign;
            return Result.success(signUrl);
        }else { // 预览封面
            Result<FileInfo> fileInfo = fileServiceClient.getFileInfo(id, uid);
            if (fileInfo.getCode() != StatusCodeEnum.SUCCESS.getCode()){
                return Result.error(StatusCodeEnum.ERROR);
            }
            if (fileInfo.getData() == null){
                return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
            }
            FileInfo file = fileInfo.getData();
            if (file.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
                return Result.error(StatusCodeEnum.FILE_CANNOT_PREVIEW);
            }
            if (file.getCover() == null || file.getTransformed() != FileConstants.TRANSFORMED_END){
                log.info("sign cover id: {}", id);
                return Result.error(StatusCodeEnum.FILE_TRANSCODING);
            }
            String fileUrl = file.getCover();
            // expires 过期时间，时间戳+8h
            long expires = System.currentTimeMillis() + 8 * 60 * 60 * 1000;
            String sign = SignUtil.sign(uid, fileUrl, expires);
            String signUrl = apiCover;
            signUrl += fileUrl + "?" + "expire=" + expires + "&uid=" + uid + "&sign=" + sign;
            return Result.success(signUrl);
        }
    }


    @Override
    public void previewVideo(HttpServletRequest request, HttpServletResponse response, String url, Long uid, Long expire, String sign) {
        boolean checkSign = SignUtil.checkSign(uid, url, expire, sign);
        if (!checkSign){
            writerResponse(response, StatusCodeEnum.PARAM_ERROR);
            return;
        }

        String range = request.getHeader("Range");
        range = range == null ? "bytes=0-" : range;
        String[] split = range.replace("bytes=", "").split("-");
        long start = Long.parseLong(split[0]);
        long end = split.length > 1 ? Long.parseLong(split[1]) : start + DEFAULT_LENGTH - 1;
        minioService.previewVideo(url, response, start, end);
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
