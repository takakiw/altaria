package com.altaria.file.service.impl;


import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.altaria.common.constants.FileConstants;
import com.altaria.common.enums.FileType;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.common.pojos.file.entity.Space;
import com.altaria.file.cache.FileCacheService;
import com.altaria.file.mapper.FileInfoMapper;
import com.altaria.file.service.FileInfoService;
import com.altaria.file.service.SpaceService;
import com.altaria.minio.service.MinioService;
import com.github.pagehelper.Page;

import com.github.pagehelper.PageHelper;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Slf4j
@Service
public class FileInfoServiceImpl implements FileInfoService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private MinioService minioService;


    @Autowired
    private SpaceService spaceService;



    @Autowired()
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private static final Long DEFAULT_LENGTH = 1024 * 1024 * 2L; // 2M



    @Override
    public Result mkdir(Long uid,Long pid, String dirName) {
        if (pid == null || uid == null) {
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        if (StringUtils.isBlank(dirName) || !dirName.matches(FileConstants.INVALID_DIR_NAME_REGEX)){
            return Result.error(StatusCodeEnum.ILLEGAL_FILE_NAME);
        }
        if(pid.compareTo(FileConstants.ROOT_DIR_ID) != 0){
            FileInfo dbFile = fileInfoMapper.getFileById(pid, uid);
            if (dbFile == null) {
                    return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
            }
            if (!dbFile.getType().equals(FileType.DIRECTORY.getType())) {
                return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
            }
        }
        FileInfo fileInfoByName = fileInfoMapper.getFileChildName(pid, uid, dirName);
        if (fileInfoByName != null) {
            return Result.error(StatusCodeEnum.FILE_ALREADY_EXISTS);
        }
        FileInfo fileInfo = new FileInfo();
        fileInfo.setId(IdUtil.getSnowflake().nextId());
        fileInfo.setUid(uid);
        fileInfo.setFileName(dirName);
        fileInfo.setType(FileType.DIRECTORY.getType());
        fileInfo.setPid(pid);
        fileInfo.setStatus(FileConstants.STATUS_USE);
        fileInfo.setSize(0L);
        int insert = fileInfoMapper.insert(fileInfo);
        if (insert > 0) {
            return Result.success(StatusCodeEnum.SUCCESS);
        }
        return Result.error(StatusCodeEnum.ERROR);
    }

    @Override
    public Result moveFile(FileInfo fileInfo, Long uid) {
        if (fileInfo.getId() == null || uid == null || fileInfo.getPid() == null) {
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        FileInfo existFile = fileInfoMapper.getFileById(fileInfo.getId(), uid);
        if (existFile == null){
                return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        if(existFile.getPid().compareTo(fileInfo.getPid()) == 0){
            return Result.success("文件已在目标目录");
        }
        // 非根目录
        if (fileInfo.getPid().compareTo(FileConstants.ROOT_DIR_ID)!= 0) {
            // 目标目录不能是自己的子目录
            if(!checkPath(fileInfo.getPid(), existFile.getUid(), existFile.getId())){
                return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
            }
            FileInfo pFile = fileInfoMapper.getFileById(fileInfo.getPid(), uid);
            if (pFile == null){
                    return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
            }
        }
        // 查询是否存在同名文件
        FileInfo dbFile = fileInfoMapper.getFileChildName(fileInfo.getPid(), uid, existFile.getFileName());
        if (dbFile != null) {
            String newFileName = existFile.getFileName() + "_(rename)";
            existFile.setFileName(newFileName);
        }
        FileInfo updateFile = new FileInfo();
        updateFile.setId(existFile.getId());
        updateFile.setFileName(existFile.getFileName());
        updateFile.setPid(fileInfo.getPid());
        updateFile.setUid(uid);
        int update = fileInfoMapper.update(updateFile);
        if (update > 0) {
            return Result.success(StatusCodeEnum.SUCCESS);
        }
        return Result.error(StatusCodeEnum.ERROR);
    }

    private boolean checkPath(Long pid, Long uid, Long id) {
        Stack<Long> stack = new Stack<>();
        stack.push(pid);
        while (!stack.isEmpty()) {
            Long currentId = stack.pop();
            if (currentId.compareTo(id) == 0){
                return false;
            }
            FileInfo fileInfo = fileInfoMapper.getFileById(currentId, uid);
            if (fileInfo == null){
                return false;
            }
            if (fileInfo.getPid().compareTo(FileConstants.ROOT_DIR_ID) != 0){
                stack.push(fileInfo.getPid());
            }
        }
        return true;
    }

    @Override
    public Result renameFile(FileInfo fileInfo, Long uid) {
        if (fileInfo.getId() == null || uid == null ){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        if (StringUtils.isBlank(fileInfo.getFileName()) || !fileInfo.getFileName().matches(FileConstants.INVALID_DIR_NAME_REGEX)){
            return Result.error(StatusCodeEnum.ILLEGAL_FILE_NAME);
        }
        FileInfo existFile = fileInfoMapper.getFileById(fileInfo.getId(), uid);
        if (existFile == null){
            return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        if (existFile.getFileName().equals(fileInfo.getFileName())){
            return Result.success(StatusCodeEnum.SUCCESS);
        }
        // 查询是否存在同名文件
        FileInfo dbFile = fileInfoMapper.getFileChildName(existFile.getPid(), existFile.getUid(), fileInfo.getFileName());
        if (dbFile != null) {
            return Result.error(StatusCodeEnum.FILE_ALREADY_EXISTS);
        }
        FileInfo updateFile = new FileInfo();
        updateFile.setId(existFile.getId());
        updateFile.setFileName(fileInfo.getFileName());
        updateFile.setUid(existFile.getUid());
        int update = fileInfoMapper.update(updateFile);
        if (update > 0) {
            return Result.success(StatusCodeEnum.SUCCESS);
        }
        return Result.error(StatusCodeEnum.ERROR);
    }

    @Override
    public Result<PageResult<FileInfo>> getPagedFileList(Long id, Long uid,Integer type, String fileName,Integer status, Integer page, Integer count) {
        if (uid == null) {
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        if (id.compareTo(FileConstants.ROOT_DIR_ID) != 0){
            FileInfo dbFile = fileInfoMapper.getFileById(id, uid);
            if (dbFile == null) {
                return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
            }
            if (!dbFile.getType().equals(FileType.DIRECTORY.getType())){
                return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
            }
        }
        FileInfo query = new FileInfo();
        query.setUid(uid);
        query.setPid(id);
        query.setType(type);
        query.setFileName(fileName);
        query.setStatus(status);
        PageHelper.startPage(page, count);
        Page<FileInfo> infos = fileInfoMapper.select(query);
        return Result.success(new PageResult<FileInfo>(infos.getTotal(), infos.getResult()));
    }

    @Override
    public Result getPath(Long id, Long uid) {
        if (id == null || uid == null) {
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        List<FileInfo> path = new ArrayList<>();
        while (id.compareTo(FileConstants.ROOT_DIR_ID) != 0) {
            FileInfo fileInfo = fileInfoMapper.getFileById(id, uid);
            if (fileInfo == null || !fileInfo.getType().equals(FileType.DIRECTORY.getType())){
                return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
            }
            path.add(0, fileInfo);
            id = fileInfo.getPid();
        }
        FileInfo root = fileInfoMapper.getFileById(FileConstants.ROOT_DIR_ID, null);
        path.add(0, root);
        return Result.success(path);
    }

    @Override
    public void preview(HttpServletResponse response, Long id, Long uid) {
        /*if (id == null || uid == null) {
            writerResponse(response, StatusCodeEnum.PARAM_NOT_NULL);
        }*/
        FileInfo dbFile = fileInfoMapper.getFileById(id, uid);
        if (dbFile == null
                || dbFile.getType().equals(FileType.DIRECTORY.getType())
                || !dbFile.getType().equals(FileType.VIDEO.getType())) {
            writerResponse(response, StatusCodeEnum.FILE_CANNOT_PREVIEW); // 文件不能预览
            return;
        }
        if (dbFile.getUrl() == null){
            writerResponse(response, StatusCodeEnum.FILE_TRANSCODING);
        }
        minioService.preview(dbFile.getUrl(), response);
    }

    @Override
    public void previewVideo(HttpServletRequest request, HttpServletResponse response, Long id, Long uid) {
        /*if (id == null || uid == null) {
            writerResponse(response, StatusCodeEnum.PARAM_NOT_NULL);
        }*/
        FileInfo dbFile = fileInfoMapper.getFileById(id, uid);
        if (dbFile == null
                || dbFile.getType().equals(FileType.DIRECTORY.getType())
                || !dbFile.getType().equals(FileType.VIDEO.getType())) {
            writerResponse(response, StatusCodeEnum.VIDEO_NOT_EXISTS);
            return;
        }
        if (dbFile.getUrl() == null){
            writerResponse(response, StatusCodeEnum.FILE_TRANSCODING);
        }
        String range = request.getHeader("Range");
        range = range == null ? "bytes=0-" : range;
        String[] split = range.replace("bytes=", "").split("-");
        long start = Long.parseLong(split[0]);
        long end = split.length > 1 ? Long.parseLong(split[1]) : start + DEFAULT_LENGTH - 1;
        minioService.previewVideo(dbFile.getUrl(), response, start, end);
    }

    @Override
    public void download(HttpServletResponse response, Long id, Long uid) {
        /*if (id == null || uid == null) {
            writerResponse(response, StatusCodeEnum.PARAM_NOT_NULL);
            return;
        }*/
        FileInfo dbFile = fileInfoMapper.getFileById(id, uid);
        if (dbFile == null || dbFile.getType().equals(FileType.DIRECTORY.getType())) {
            writerResponse(response, StatusCodeEnum.FILE_NOT_EXISTS);
        }
        if (dbFile.getUrl() == null){
            writerResponse(response, StatusCodeEnum.FILE_TRANSCODING);
        }
        minioService.downloadFile(dbFile.getUrl(), response);
    }

    @Override
    public Result deleteFile(List<Long> ids, Long uid) {
        if (ids == null || uid == null) {
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        List<FileInfo> dbFiles = fileInfoMapper.getFileByIds(ids, uid, FileConstants.STATUS_USE);
        if (dbFiles == null || dbFiles.isEmpty()){
            return Result.success();
        }
        ids = dbFiles.stream().map(FileInfo::getId).toList();
        List<Long> pids = dbFiles.stream().map(FileInfo::getPid).toList();
        fileInfoMapper.updateStatusBatch(ids, FileConstants.STATUS_DELETE, LocalDateTime.now());
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result removeFile(List<Long> ids, Long uid) {
        if (ids == null || uid == null) {
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        List<FileInfo> dbFiles = fileInfoMapper.getFileByIds(ids, uid, null);
        if (dbFiles == null || dbFiles.isEmpty()){
            return Result.success();
        }
        long size = 0;
        Stack<FileInfo> parents = new Stack<>();
        parents.addAll(dbFiles);
        ids = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        while (!parents.isEmpty()) {
            FileInfo fileInfo = parents.pop();
            size += fileInfo.getSize();
            ids.add(fileInfo.getId());
            if (fileInfo.getUrl() != null){
                urls.add(fileInfo.getUrl());
            }
            if (fileInfo.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
                List<FileInfo> childFiles = fileInfoMapper.getChildFiles(fileInfo.getId(), uid);
                if (!childFiles.isEmpty()){
                    parents.addAll(childFiles);
                }
            }
        }
        minioService.deleteFile(urls);
        fileInfoMapper.deleteBatch(ids, uid);
        spaceService.updateSpace(uid, -size, -ids.size());
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result upload(Long uid, Long pid, MultipartFile file, String md5, Integer index, Integer total) {
        if (uid == null || file == null || file.isEmpty()){
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        if (index == 0){
            FileInfo fileByMd5 = fileInfoMapper.getFileByMd5(md5, uid);
            if (fileByMd5 != null){
                FileInfo dbFile = new FileInfo();
                dbFile.setId(IdUtil.getSnowflake(1, 1).nextId());
                dbFile.setUrl(fileByMd5.getUrl());
                dbFile.setMd5(fileByMd5.getMd5());
                dbFile.setFileName(file.getOriginalFilename());
                dbFile.setPid(pid);
                dbFile.setUid(uid);
                fileInfoMapper.insert(dbFile);
                spaceService.updateSpace(uid, fileByMd5.getSize(),1);
                return Result.success(100);
            }
        }
        else if (index == total - 1){

        }else {

        }

        // TODO 更新用户使用的空间大小
        return null;
    }



    public void writerResponse(HttpServletResponse response, StatusCodeEnum statusCode){
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(JSONObject.toJSONString(Result.error(statusCode)));
        } catch (IOException e) {
            log.error("请求头设置失败", e);
        }
    }
}
