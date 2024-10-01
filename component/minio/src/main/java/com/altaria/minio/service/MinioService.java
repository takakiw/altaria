package com.altaria.minio.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.scheduling.annotation.Async;

import java.io.InputStream;

public interface MinioService {




    void upLoadFile(String fileName, InputStream inputStream, String contentType);


    void deleteFile(String fileName);

    void deleteFile(String[] fileNames);

    void downloadFile(String fileName, HttpServletResponse response);

    public void previewVideo(String fileName, HttpServletResponse response, long start, long end);


}
