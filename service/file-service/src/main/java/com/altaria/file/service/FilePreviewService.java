package com.altaria.file.service;

import com.altaria.common.pojos.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface FilePreviewService {

    void preview(HttpServletResponse response, Long id, Long uid, Long expire, String sign);

    void previewVideo(HttpServletRequest request, HttpServletResponse response, Long id, Long uid, Long expire, String sign);

    void previewCover(HttpServletResponse response, Long id, Long uid, Long expire, String sign);

    Result<String> sign(Long id, Long uid, String category);

}
