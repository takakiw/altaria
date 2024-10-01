package com.altaria.minio.service.impl;

import com.altaria.minio.service.MinioService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MinioServiceImpl implements MinioService {


    private static final int DEFAULT_LENGTH = 1024 * 1024 * 2; // 默认下载文件大小为1MB

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String default_bucketName;




    @Override
    public void upLoadFile(String fileName, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(default_bucketName)
                            .contentType(contentType)
                            .object(fileName)
                            .stream(inputStream, inputStream.available(), -1)
                            .build()
            );
            log.info("成功上传文件 {} 到 {}", fileName, default_bucketName);
        } catch (Exception e) {
            log.error("minio文件上传失败", e);
            throw new RuntimeException("minio文件上传失败", e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(default_bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("成功删除文件 {}", fileName);
        } catch (Exception e) {
            log.error("minio文件删除失败", e);
            throw new RuntimeException("minio文件删除失败", e);
        }
    }

    @Override
    public void deleteFile(String[] fileNames) {
        // 检查输入参数
        if (fileNames == null || fileNames.length == 0) {
            log.warn("没有提供要删除的文件名");
            return;
        }

        List<DeleteObject> deleteObjects = new ArrayList<>();
        for (String fileName : fileNames) {
            deleteObjects.add(new DeleteObject(fileName));
        }

        try {
            minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(default_bucketName)
                    .objects(deleteObjects)
                    .build());
            log.info("成功删除 {} 个文件", fileNames.length);
        } catch (Exception e) {
            log.error("minio文件批量删除失败", e);
            throw new RuntimeException("minio文件批量删除失败", e);
        }
    }

    @Override
    public void downloadFile(String fileName, HttpServletResponse response) {
        try{
            GetObjectResponse object = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(default_bucketName)
                            .object(fileName)
                            .build()
            );
            object.transferTo(response.getOutputStream());
        }catch (Exception e){
            log.warn("文件不存在！");
        }
    }


    @Override
    public void previewVideo(String fileName, HttpServletResponse response, long start, long end) {
        StatObjectResponse statObject = null;
        try {
            statObject = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(default_bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error("minio文件{}元数据获取失败", fileName);
            throw new RuntimeException(e);
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
        // 从 MinIO 中读取数据并写入响应
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket("mini")
                        .object(fileName)
                        .offset(start)
                        .length(end - start + 1)
                        .build())) {
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("获取{}视频流失败!", fileName);
            throw new RuntimeException(e);
        }
    }
}
