package com.altaria.minio.service.impl;

import com.altaria.minio.config.MinioConfig;
import com.altaria.minio.config.MinioProperties;
import com.altaria.minio.service.MinioService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Async;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Import(MinioConfig.class)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioServiceImpl implements MinioService {


    private static final Long DEFAULT_LENGTH = 1024 * 1024 / 2L; // 默认下载文件大小为 512KB

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;




    @Override
    public void upLoadFile(String fileName, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .contentType(contentType)
                            .object(fileName)
                            .stream(inputStream, inputStream.available(), -1)
                            .build()
            );
            log.info("成功上传文件 {} 到 {}", fileName, minioProperties.getBucketName());
            return;
        } catch (Exception e) {
            log.error("minio文件上传失败", e);
            return;
        }
    }

    @Override
    public void deleteFile(String fileName) throws RuntimeException{
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build()
            );
            System.out.println(minioProperties);
            log.info("成功删除文件 {}", fileName);
        } catch (Exception e) {
            log.error("minio文件删除失败", e);
        }
    }

    @Override
    public void deleteFile(List<String> fileNames) {
        // 检查输入参数
        if (fileNames == null || fileNames.size() == 0) {
            log.warn("没有提供要删除的文件名");
        }

        List<DeleteObject> deleteObjects = new ArrayList<>();
        for (String fileName : fileNames) {
            deleteObjects.add(new DeleteObject(fileName));
        }
        Iterable<Result<DeleteError>> results =
                minioClient.removeObjects(
                        RemoveObjectsArgs.builder().
                                bucket(minioProperties.getBucketName()).
                                objects(deleteObjects).
                                build());

        try {
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("minio批量删除文件失败：" + error.objectName() + "; " + error.message());
            }
        } catch (Exception e) {
            log.error("minio文件批量删除失败", e);
        }
    }

    @Override
    public void downloadFile(String fileName, HttpServletResponse response) {
        StatObjectResponse statObject = null;
        try {
            statObject = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error("minio文件{}元数据获取失败", fileName);
            writerResponse(response, 404, "文件不存在！");
            return;
        }
        if (statObject == null){
            log.warn("文件不存在！");
            writerResponse(response, 404, "文件不存在！");
            return;
        }
        // 设置响应头
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setHeader("Content-Length", String.valueOf(statObject.size()));
        response.setContentType(statObject.contentType()); // 根据文件格式设置
        response.setCharacterEncoding("utf-8");

        try (GetObjectResponse object = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build()
            )) {
            object.transferTo(response.getOutputStream());
        }catch (Exception e){
            log.warn("文件不存在！");
            writerResponse(response, 404, "文件不存在！");
            return;
        }
    }

    @Override
    public void preview(String fileName, HttpServletResponse response) {
        StatObjectResponse statObject = null;
        if (response.isCommitted()){
            return;
        }
        try {
            statObject = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error("minio文件{}元数据获取失败", fileName);
            writerResponse(response, 404, "文件不存在！");
            return;
        }
        // 设置响应头
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Content-Length", String.valueOf(statObject.size()));
        response.setContentType(statObject.contentType()); // 根据视频格式设置
        response.setCharacterEncoding("utf-8");
        if (response.isCommitted()){
            return;
        }
        // 从 MinIO 中读取数据并写入响应
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(fileName)
                        .build())) {
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
            inputStream.close();
        } catch (Exception e) {
            log.error("获取{}文件流失败!", fileName);
            if (response.isCommitted()){
                return;
            }
            writerResponse(response, 500, "获取文件流失败!");
            return;
        }
    }




    @Override
    public void previewVideo(String fileName, HttpServletResponse response, long start, long end) {
        StatObjectResponse statObject = null;
        if (response.isCommitted()){
            return;
        }
        try {
            statObject = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error("minio文件{}元数据获取失败", fileName);
            writerResponse(response, 404, "文件不存在！");
            return;
        }
        long fileSize = statObject.size();
        if (start >= fileSize) {
            response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }
        end = Math.min(fileSize - 1, Math.min(end, start + DEFAULT_LENGTH - 1));
        // 设置响应头
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
        response.setHeader("Content-Length", String.valueOf(end - start + 1));
        response.setContentType(statObject.contentType()); // 根据视频格式设置
        response.setCharacterEncoding("utf-8");
        if (response.isCommitted()){
            return;
        }
        // 从 MinIO 中读取数据并写入响应
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(fileName)
                        .offset(start)
                        .length(end - start + 1)
                        .build())) {
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
            inputStream.close();
        } catch (Exception e) {
            log.error("获取{}视频流失败!", fileName);
            if (response.isCommitted()){
                return;
            }
            writerResponse(response, 500, "获取视频流失败!");
            return;
        }
    }

    private void writerResponse(HttpServletResponse response, int i, String s) {
        if (response.isCommitted()){
            return;
        }
        response.setStatus(i);
        response.setContentType("text/plain;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        try {
            // 判断连接是否关闭
            if (response.isCommitted()){
                return;
            }
            response.getWriter().write(s);
        } catch (Exception e) {
            log.error("响应写入失败", e);
        }
    }
}
