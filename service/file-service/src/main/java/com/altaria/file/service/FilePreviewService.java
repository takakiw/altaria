package com.altaria.file.service;

import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface FilePreviewService {

    void preview(HttpServletResponse response, Long id, Long uid);

    void previewVideo(HttpServletRequest request, HttpServletResponse response, Long id, Long uid);

    void previewCover(HttpServletResponse response, Long id, Long uid);

}
