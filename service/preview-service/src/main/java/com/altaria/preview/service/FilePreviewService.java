package com.altaria.preview.service;

import com.altaria.common.pojos.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface FilePreviewService {

    void preview(HttpServletResponse response, String url, Long uid, Long expire, String sign);

    void previewVideo(HttpServletRequest request, HttpServletResponse response, String url, Long uid, Long expire, String sign);

    void previewCover(HttpServletResponse response, String url, Long uid, Long expire, String sign);

    Result<String> sign(Long id, Long uid, String category);

}
