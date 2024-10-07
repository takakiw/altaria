package com.altaria.minio.service;



import jakarta.servlet.http.HttpServletResponse;
import org.springframework.scheduling.annotation.Async;

import java.io.InputStream;
import java.util.List;

public interface MinioService {




    void upLoadFile(String fileName, InputStream inputStream, String contentType);


    @Async
    boolean deleteFile(String fileName);

    @Async
    boolean deleteFile(List<String> fileNames);

    void downloadFile(String fileName, HttpServletResponse response);

    void previewVideo(String fileName, HttpServletResponse response, long start, long end);


}
