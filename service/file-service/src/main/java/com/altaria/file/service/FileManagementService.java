package com.altaria.file.service;

import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.common.pojos.file.entity.MoveFile;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileManagementService {

    Result mkdir(Long uId, Long pid, String dirName);

    Result moveFile(MoveFile fileInfo, Long uid);

    Result renameFile(FileInfo fileInfo, Long uid);

    Result<PageResult<FileInfo>> getPagedFileList(Long id, Long uid, Integer type, String fileName, Integer order);

    Result<List<FileInfo>> getPath(Long id, Long uid);

    void download(HttpServletResponse response, Long id, Long uid);

    Result deleteFile(List<Long> ids, Long uid);

    Result removeFile(List<Long> ids, Long uid);

    Result upload(Long uid, Long fid, Long pid, MultipartFile file, String md5, Integer index, Integer total);

    Result restoreFile(List<Long> ids, Long uid);

    Result<PageResult<FileInfo>> getRecycleFileList(Long uid);

    String uploadImage(MultipartFile file);

    Result<List<FileInfo>> getFileInfoBatch(List<Long> fids, Long uid);

    Result saveFileToCloud(List<Long> fids, Long shareUid, Long path, Long userId);
}
