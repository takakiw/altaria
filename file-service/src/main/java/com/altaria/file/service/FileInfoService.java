package com.altaria.file.service;

import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileInfoService {
    Result mkdir(Long uId, Long pid, String dirName);

    Result moveFile(FileInfo fileInfo, Long uid);

    Result renameFile(FileInfo fileInfo, Long uid);

    Result<PageResult<FileInfo>> getPagedFileList(Long id, Long uid, Integer type,String fileName, Integer status, Integer page, Integer count);

    Result getPath(Long id, Long uid);

    void preview(HttpServletResponse response, Long id, Long uid);

    void previewVideo(HttpServletRequest request, HttpServletResponse response, Long id, Long uid);

    void download(HttpServletResponse response, Long id, Long uid);

    Result deleteFile(List<Long> ids, Long uid);

    Result removeFile(List<Long> ids, Long uid);

    Result upload(Long uid, MultipartFile file, String md5, Integer index, Integer total);
}
