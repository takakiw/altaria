package com.altaria.minio.service;



import jakarta.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.util.List;

public interface MinioService {




    void upLoadFile(String fileName, InputStream inputStream, String contentType);
    void upLoadFile(String fileName, InputStream inputStream, String contentType, String bucketName);


    void deleteFile(String fileName);

    void deleteFile(String fileName, String bucketName);

    void deleteFile(List<String> fileNames);

    void downloadFile(String fileName, HttpServletResponse response);
    void preview(String fileName, HttpServletResponse response);

    void previewVideo(String fileName, HttpServletResponse response, long start, long end);
}
