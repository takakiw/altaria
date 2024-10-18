package com.altaria.file.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.altaria.common.constants.FileConstants;
import com.altaria.common.constants.MinioConstants;
import com.altaria.common.enums.FileType;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.common.pojos.file.entity.Space;
import com.altaria.common.pojos.file.mq.UploadMQType;
import com.altaria.file.cache.FileCacheService;
import com.altaria.file.mapper.FileInfoMapper;
import com.altaria.file.service.FileManagementService;
import com.altaria.file.service.SpaceManagementService;
import com.altaria.minio.service.MinioService;
import com.github.pagehelper.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
public class FileManagementServiceImpl implements FileManagementService {


    @Autowired
    private FileCacheService cacheService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SpaceManagementService spaceManagementService;

    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;

    @Value("${temp.file.path:/temp/file/}")
    private String tempPath;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private MinioService minioService;

    @Override
    public String uploadImage(MultipartFile file) {
        String prefix = UUID.randomUUID().toString().replace("-", "");
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = prefix + suffix;
        try {
            minioService.upLoadFile(fileName, file.getInputStream(), file.getContentType(), MinioConstants.AVATAR_BUCKET_NAME);
        } catch (Exception e) {
            return null;
        }
        return fileName;
    }



    @Override
    public Result mkdir(Long uid, Long pid, String dirName) {
        if (pid == null || uid == null) {
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        if (StringUtils.isBlank(dirName) || !dirName.matches(FileConstants.INVALID_DIR_NAME_REGEX)){
            return Result.error(StatusCodeEnum.ILLEGAL_FILE_NAME);
        }
        if(pid.compareTo(FileConstants.ROOT_DIR_ID) != 0){
            FileInfo parentFile = cacheService.getFile(uid, pid);
            if (parentFile == null){
                parentFile = fileInfoMapper.getFileById(pid, uid);
                if (parentFile == null){
                    cacheService.saveNullFile(uid, pid);
                    return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
                }
                cacheService.saveFile(parentFile);
            }
            if (parentFile.getId() == null || parentFile.getType().compareTo(FileType.DIRECTORY.getType()) != 0){
                return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
            }
        }
        RLock lock = redissonClient.getLock("fileLock" + pid + ":" + uid + ":" + dirName);
        try {
            boolean b = lock.tryLock(10, 10, TimeUnit.MILLISECONDS);
            if (!b){
                return Result.error(StatusCodeEnum.ERROR);
            }
            if (cacheService.ParentKeyCodeExists(uid, pid)){
                Boolean isName = cacheService.getChildByFileName(uid, pid, dirName);
                if (isName){
                    return Result.error(StatusCodeEnum.FILE_ALREADY_EXISTS);
                }
            }else {
                FileInfo fileChildName = fileInfoMapper.getFileChildName(pid, uid, dirName);
                if (fileChildName != null){
                    cacheService.saveFile(fileChildName);
                    return Result.error(StatusCodeEnum.FILE_ALREADY_EXISTS);
                }
            }
            FileInfo fileInfo = new FileInfo();
            fileInfo.setId(IdUtil.getSnowflake().nextId());
            fileInfo.setUid(uid);
            fileInfo.setFileName(dirName);
            fileInfo.setType(FileType.DIRECTORY.getType());
            fileInfo.setPid(pid);
            fileInfo.setStatus(FileConstants.STATUS_USE);
            fileInfo.setSize(0L);
            fileInfo.setTransformed(FileConstants.TRANSFORMED_END);
            int insert = fileInfoMapper.insert(fileInfo);
            if (insert > 0) {
                cacheService.addChildren(uid, pid, fileInfo);
                cacheService.saveFile(fileInfo);
                return Result.success(StatusCodeEnum.SUCCESS);
            }
            return Result.error(StatusCodeEnum.ERROR);
        } catch (InterruptedException e) {
            return Result.error(StatusCodeEnum.ERROR);
        }finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result moveFile(FileInfo fileInfo, Long uid) {
        if (fileInfo.getId() == null || uid == null || fileInfo.getPid() == null) {
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        FileInfo file = cacheService.getFile(uid, fileInfo.getId());
        if (file == null){
            file = fileInfoMapper.getFileById(fileInfo.getId(), uid);
            if (file == null){
                cacheService.saveNullFile(uid, fileInfo.getId());
                return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
            }
            cacheService.saveFile(file);
        }
        if (file.getId() == null){
            return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        if(file.getPid().compareTo(fileInfo.getPid()) == 0){
            return Result.success("文件已在目标目录");
        }
        // 非根目录
        if (fileInfo.getPid().compareTo(FileConstants.ROOT_DIR_ID)!= 0) {
            // 目标目录不能是自己的子目录
            if(!checkPath(fileInfo.getPid(), uid, file.getId())){
                return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
            }
            FileInfo parentFile = cacheService.getFile(uid, fileInfo.getPid());
            if (parentFile == null){
                parentFile = fileInfoMapper.getFileById(fileInfo.getPid(), uid);
                if (parentFile == null){
                    cacheService.saveNullFile(uid, fileInfo.getPid());
                    return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
                }
                cacheService.saveFile(parentFile);
            }
            if (parentFile.getId() == null || parentFile.getType().compareTo(FileType.DIRECTORY.getType()) != 0){
                return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
            }
        }
        // 查询是否存在同名文件
        if (cacheService.ParentKeyCodeExists(fileInfo.getUid(), fileInfo.getPid())){
            if (cacheService.getChildByFileName(fileInfo.getUid(), fileInfo.getPid(), file.getFileName())){
                file.setFileName(fileInfo.getFileName() + "("+ RandomUtil.randomString(2) +")");
            }
        }else {
            FileInfo fileChildName = fileInfoMapper.getFileChildName(fileInfo.getPid(), uid, file.getFileName());
            if (fileChildName != null){
                file.setFileName(fileInfo.getFileName() + "("+ RandomUtil.randomString(2) +")");
                cacheService.saveFile(fileChildName);
            }
        }
        FileInfo updateFile = new FileInfo();
        updateFile.setId(file.getId());
        updateFile.setFileName(file.getFileName());
        updateFile.setPid(fileInfo.getPid());
        updateFile.setUid(uid);
        int update = fileInfoMapper.update(updateFile);
        // 更新父目录大小
        pathAddSize(uid, fileInfo.getPid(), file.getSize());
        pathAddSize(uid, file.getPid(), -file.getSize());
        if (update > 0) {
            cacheService.deleteChildren(uid, file.getPid(), file.getId());
            file.setPid(fileInfo.getPid());
            file.setUpdateTime(LocalDateTime.now());
            cacheService.addChildren(uid, fileInfo.getPid(), file);
            cacheService.saveFile(file);
            return Result.success(StatusCodeEnum.SUCCESS);
        }
        return Result.error(StatusCodeEnum.ERROR);
    }

    public void pathAddSize(Long uid, Long pid, Long size){
        while (pid.compareTo(FileConstants.ROOT_DIR_ID) != 0){
            FileInfo parentFile = cacheService.getFile(uid, pid);
            if (parentFile != null && parentFile.getId() != null){
                fileInfoMapper.updateParentSize(uid, parentFile.getId(), size);
                cacheService.updateFileSize(uid, parentFile.getId(), parentFile.getPid(), size);
                pid = parentFile.getPid();
            }else {
                parentFile = fileInfoMapper.getFileById(pid, uid);
                if (parentFile == null){
                    break;
                }
                fileInfoMapper.updateParentSize(parentFile.getUid(), parentFile.getId(), size);
                parentFile.setSize(parentFile.getSize() + size);
                cacheService.saveFile(parentFile);
                pid = parentFile.getPid();
            }
        }
    }

    private boolean checkPath(Long pid, Long uid, Long id) {
        Stack<Long> stack = new Stack<>();
        stack.push(pid);
        while (!stack.isEmpty()) {
            Long currentId = stack.pop();
            if (currentId.compareTo(id) == 0){
                return false;
            }
            FileInfo file = cacheService.getFile(uid, currentId);
            if (file == null){
                file = fileInfoMapper.getFileById(currentId, uid);
                if (file == null){
                    cacheService.saveNullFile(uid, currentId);
                    return false;
                }
                cacheService.saveFile(file);
            }
            if (file.getId() == null || file.getType().compareTo(FileType.DIRECTORY.getType()) != 0){
                return false;
            }
            if (file.getPid().compareTo(FileConstants.ROOT_DIR_ID) != 0){
                stack.push(file.getPid());
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
        FileInfo file = cacheService.getFile(uid, fileInfo.getId());
        if (file == null){
            file = fileInfoMapper.getFileById(fileInfo.getId(), uid);
            if (file == null){
                cacheService.saveNullFile(uid, fileInfo.getId());
                return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
            }
            cacheService.saveFile(file);
        }
        if (file.getId() == null){
            return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        if (cacheService.ParentKeyCodeExists(file.getUid(), file.getPid())){
            if (cacheService.getChildByFileName(file.getUid(), file.getPid(), fileInfo.getFileName())){
                return Result.error(StatusCodeEnum.FILE_ALREADY_EXISTS);
            }
        }else {
            FileInfo fileChildName = fileInfoMapper.getFileChildName(file.getPid(), uid, fileInfo.getFileName());
            if (fileChildName != null){
                cacheService.saveFile(fileChildName);
                return Result.error(StatusCodeEnum.FILE_ALREADY_EXISTS);
            }
        }
        FileInfo updateFile = new FileInfo();
        updateFile.setId(file.getId());
        updateFile.setFileName(fileInfo.getFileName());
        updateFile.setUid(file.getUid());
        int update = fileInfoMapper.update(updateFile);
        if (update > 0) {
            String oldName = file.getFileName();
            file.setFileName(fileInfo.getFileName());
            file.setUpdateTime(LocalDateTime.now());
            cacheService.saveFile(file);
            cacheService.updateFileName(uid, file.getPid(), file.getId(), file.getFileName(), oldName);
            return Result.success(StatusCodeEnum.SUCCESS);
        }
        return Result.error(StatusCodeEnum.ERROR);
    }

    @Override
    public Result<PageResult<FileInfo>> getPagedFileList(Long id, Long uid, Integer type, String fileName, Integer order) {
        if (uid == null) {
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        if (id.compareTo(FileConstants.ROOT_DIR_ID) != 0){
            FileInfo file = cacheService.getFile(uid, id);
            if (file == null){
                file = fileInfoMapper.getFileById(id, uid);
                if (file == null){
                    cacheService.saveNullFile(uid, id);
                    return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
                }
                cacheService.saveFile(file);
            }
            if (file.getId() == null || file.getType().compareTo(FileType.DIRECTORY.getType()) != 0){
                return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
            }
        }
        if (cacheService.ParentKeyCodeExists(uid, id)){
            List<FileInfo> targetFiles = new ArrayList<>();
            switch (order) {
                case 0 -> targetFiles = cacheService.getChildrenOrderUpdateTimeReverse(uid, id);
                case 1 -> targetFiles = cacheService.getChildrenOrderUpdateTime(uid, id);
                case 2 -> targetFiles = cacheService.getChildrenOrderSize(uid, id);
                case 3 -> targetFiles = cacheService.getChildrenOrderSizeReverse(uid, id);
                case 4 -> {
                    List<Long> fidList = cacheService.getChildrenOrderName(uid, id);
                    targetFiles = fidList.stream().map(fid -> cacheService.getFile(uid, fid)).toList();
                }
                case 5 -> {
                    List<Long> fidListReverse = cacheService.getChildrenOrderNameReverse(uid, id);
                    targetFiles = fidListReverse.stream().map(fid -> cacheService.getFile(uid, fid)).toList();
                }
                default -> targetFiles = cacheService.getChildrenOrderUpdateTime(uid, id);
            }
            targetFiles.stream()
                    .filter(f -> type == null || f.getType().compareTo(type) == 0 && (fileName == null || f.getFileName().contains(fileName)))
                    .toList();
            return Result.success(new PageResult<>(targetFiles.size(), targetFiles));
        }else {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setPid(id);
            fileInfo.setUid(uid);
            fileInfo.setStatus(FileConstants.STATUS_USE);
            Page<FileInfo> select = fileInfoMapper.select(fileInfo);
            cacheService.saveAllChildren(uid, id, select.getResult());
            List<FileInfo> result = select.getResult();
            result.stream()
                    .filter(f -> type == null || f.getType().compareTo(type) == 0 && (fileName == null || f.getFileName().contains(fileName)))
                    .toList();
            switch (order) {
                case 0 -> result.sort((f1, f2) -> f2.getUpdateTime().compareTo(f1.getUpdateTime()));
                case 1 -> result.sort((f1, f2) -> f1.getUpdateTime().compareTo(f2.getUpdateTime()));
                case 2 -> result.sort((f1, f2) -> f1.getSize().compareTo(f2.getSize()));
                case 3 -> result.sort((f1, f2) -> f2.getSize().compareTo(f1.getSize()));
                case 4 -> result.sort((f1, f2) -> f1.getFileName().compareTo(f2.getFileName()));
                case 5 -> result.sort((f1, f2) -> f2.getFileName().compareTo(f1.getFileName()));
                default -> result.sort((f1, f2) -> f1.getUpdateTime().compareTo(f2.getUpdateTime()));
            }
            return Result.success(new PageResult<>(result.size(), result));
        }
    }

    @Override
    public Result<List<FileInfo>> getPath(Long id, Long uid) {
        if (id == null || uid == null) {
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        List<FileInfo> path = new ArrayList<>();
        while (id.compareTo(FileConstants.ROOT_DIR_ID) != 0) {
            FileInfo file = cacheService.getFile(uid, id);
            if (file == null){
                file = fileInfoMapper.getFileById(id, uid);
                if (file == null){
                    cacheService.saveNullFile(uid, id);
                    return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
                }
                cacheService.saveFile(file);
            }
            if (file.getId() == null || file.getType().compareTo(FileType.DIRECTORY.getType()) != 0 || file.getStatus().compareTo(FileConstants.STATUS_USE) != 0){
                return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
            }
            path.add(0, file);
            id = file.getPid();
        }
        FileInfo root = getRootDIR(uid);
        path.add(0, root);
        return Result.success(path);
    }



    private FileInfo getRootDIR(Long uid) {
        FileInfo root = new FileInfo();
        root.setId(FileConstants.ROOT_DIR_ID);
        root.setFileName("全部文件");
        root.setPid(-1L);
        root.setUid(uid);
        root.setType(FileType.DIRECTORY.getType());
        root.setStatus(FileConstants.STATUS_USE);
        return root;
    }

    @Override
    public Result<PageResult<FileInfo>> getRecycleFileList(Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        List<FileInfo> recycleFiles = null;
        if (cacheService.existsRecycleChildren(uid)){
            recycleFiles = cacheService.getRecycleFiles(uid);
        }else {
            recycleFiles = fileInfoMapper.getRecycleFiles(uid);
            if (recycleFiles != null && !recycleFiles.isEmpty()){
                cacheService.saveRecycleFiles(uid, recycleFiles);
            }
            if (recycleFiles == null){
                return Result.success(new PageResult<>(0, null));
            }
        }
        return Result.success(new PageResult<>(recycleFiles.size(), recycleFiles));
    }


    @Override
    public void download(HttpServletResponse response, Long id, Long uid) {
        if (id == null || uid == null) {
            writerResponse(response, StatusCodeEnum.PARAM_NOT_NULL);
            return;
        }
        FileInfo file = cacheService.getFile(uid, id);
        if (file == null){
            file = fileInfoMapper.getFileById(id, uid);
            if (file == null){
                cacheService.saveNullFile(uid, id);
                writerResponse(response, StatusCodeEnum.FILE_NOT_EXISTS);
                return;
            }
            cacheService.saveFile(file);
        }
        if (file.getId() == null || file.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
            writerResponse(response, StatusCodeEnum.ILLEGAL_REQUEST); // 文件不能下载
        }
        if (file.getUrl() == null){
            writerResponse(response, StatusCodeEnum.FILE_TRANSCODING);
            return;
        }
        minioService.downloadFile(file.getUrl(), response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Result deleteFile(List<Long> ids, Long uid) {
        if (ids == null || uid == null) {
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        List<FileInfo> dbFiles = fileInfoMapper.getFileByIds(ids, uid, FileConstants.STATUS_USE);
        cacheService.saveRecycleFiles(uid, dbFiles);
        if (dbFiles == null || dbFiles.isEmpty()){
            return Result.success();
        }
        ids = dbFiles.stream().map(FileInfo::getId).toList();
        List<Long> pids = dbFiles.stream().map(FileInfo::getPid).collect(Collectors.toSet()).stream().toList();
        if (pids.size() != 1){
            if (pids.size() == 0){
                return Result.success();
            }
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }

        // 更新父目录大小
        long size = dbFiles.stream().map(FileInfo::getSize).reduce(0L, Long::sum);
        // 缓存删除文件
        fileInfoMapper.updateStatusBatch(uid, ids, FileConstants.STATUS_RECYCLE, LocalDateTime.now());
        cacheService.deleteFileBatch(uid, ids);
        cacheService.deleteAllChildren(uid, pids.get(0));
        // 递归获取所有子文件
        Set<Long> set = new HashSet<>();
        Stack<FileInfo> stk = new Stack<>();
        stk.addAll(dbFiles);
        while (!stk.isEmpty()){
            FileInfo fileInfo = stk.pop();
            set.add(fileInfo.getId());
            if (fileInfo.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
                if (cacheService.ParentKeyCodeExists(fileInfo.getUid(), fileInfo.getId())){
                    List<FileInfo> children = cacheService.getChildrenOrderUpdateTime(uid, fileInfo.getId());
                    List<Long> longs = children.stream().map(FileInfo::getId).toList();
                    set.addAll(longs);
                    stk.addAll(children);
                    cacheService.deleteAllChildren(uid, fileInfo.getId());
                }else {
                    List<FileInfo> childFiles = fileInfoMapper.getChildFiles(fileInfo.getId(), uid, FileConstants.STATUS_USE);
                    List<Long> longs = childFiles.stream().map(FileInfo::getId).toList();
                    set.addAll(longs);
                    stk.addAll(childFiles);
                }
            }
            cacheService.deleteFile(uid, fileInfo.getId());
        }
        set.removeAll(ids);
        List<Long> dbDelFid = set.stream().toList();
        if (!dbDelFid.isEmpty()){
            fileInfoMapper.updateStatusBatch(uid, dbDelFid, FileConstants.STATUS_DELETE, LocalDateTime.now());
        }
        pathAddSize(uid, pids.get(0), -size);
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
        List<FileInfo> useFileList = dbFiles.stream().filter(f -> f.getStatus().compareTo(FileConstants.STATUS_USE) == 0).toList();
        List<Long> pidList = useFileList.stream().map(FileInfo::getPid).collect(Collectors.toSet()).stream().toList();
        int pidCount = pidList.size();
        if (pidCount > 1 ){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }

        Set<Long> set = new HashSet<>();
        Stack<FileInfo> stk = new Stack<>();
        long size = useFileList.stream().map(FileInfo::getSize).reduce(0L, Long::sum);
        if (pidCount == 1){
            pathAddSize(uid, pidList.get(0), -size);
            cacheService.deleteAllChildren(uid, pidList.get(0));
            stk.addAll(useFileList);
        }
        Map<String, String> md5UrlMap = new HashMap<>(); // 存储md5和url的映射
        Map<String, Integer> md5Count = new HashMap<>(); // 存储md5和数量的映射
        Map<String, Integer> md5Total = new HashMap<>(); // 存储md5和总数量的映射
        while (!stk.isEmpty()){
            FileInfo fileInfo = stk.pop();
            List<FileInfo> fileByMd5 = fileInfoMapper.getFileByMd5(fileInfo.getMd5());
            if (fileByMd5 != null && !fileByMd5.isEmpty()){
                md5UrlMap.put(fileInfo.getMd5(), fileByMd5.get(0).getUrl());
                md5Total.put(fileInfo.getMd5(), fileByMd5.size());
                if (md5Count.containsKey(fileInfo.getMd5())){
                    md5Count.put(fileInfo.getMd5(), md5Count.get(fileInfo.getMd5()) + 1);
                }else {
                    md5Count.put(fileInfo.getMd5(), 1);
                }
            }
            set.add(fileInfo.getId());
            if (fileInfo.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
                if (cacheService.ParentKeyCodeExists(fileInfo.getUid(), fileInfo.getId())){
                    List<FileInfo> children = cacheService.getChildrenOrderUpdateTime(uid, fileInfo.getId());
                    List<Long> longs = children.stream().map(FileInfo::getId).toList();
                    set.addAll(longs);
                    stk.addAll(children);
                    cacheService.deleteAllChildren(uid, fileInfo.getId());
                }else {
                    List<FileInfo> childFiles = fileInfoMapper.getChildFiles(fileInfo.getId(), uid, FileConstants.STATUS_USE);
                    List<Long> longs = childFiles.stream().map(FileInfo::getId).toList();
                    set.addAll(longs);
                    stk.addAll(childFiles);
                }
            }
            cacheService.deleteFile(uid, fileInfo.getId());
        }

        List<FileInfo> delFileList = dbFiles.stream().filter(f -> f.getStatus().compareTo(FileConstants.STATUS_RECYCLE) == 0).toList();
        cacheService.deleteRecycleFiles(uid, delFileList.stream().map(FileInfo::getId).toList());
        size += delFileList.stream().map(FileInfo::getSize).reduce(0L, Long::sum);
        stk.addAll(delFileList);
        while (!stk.isEmpty()){
            FileInfo fileInfo = stk.pop();
            List<FileInfo> fileByMd5 = fileInfoMapper.getFileByMd5(fileInfo.getMd5());
            if (fileByMd5 != null && !fileByMd5.isEmpty()){
                md5UrlMap.put(fileInfo.getMd5(), fileByMd5.get(0).getUrl());
                md5Total.put(fileInfo.getMd5(), fileByMd5.size());
                if (md5Count.containsKey(fileInfo.getMd5())){
                    md5Count.put(fileInfo.getMd5(), md5Count.get(fileInfo.getMd5()) + 1);
                }else {
                    md5Count.put(fileInfo.getMd5(), 1);
                }
            }
            set.add(fileInfo.getId());
            if (fileInfo.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
                List<FileInfo> childFiles = fileInfoMapper.getChildFiles(fileInfo.getId(), uid, FileConstants.STATUS_DELETE);
                set.addAll(childFiles.stream().map(FileInfo::getId).toList());
                stk.addAll(childFiles);
            }
        }
        if (set.isEmpty()){
            return Result.success();
        }
        fileInfoMapper.deleteBatch(set.stream().toList(), uid);
        List<String> delUrls = new ArrayList<>();
        for (String md5 : md5UrlMap.keySet()){
            if (Objects.equals(md5Count.get(md5), md5Total.get(md5))){
                delUrls.add(md5UrlMap.get(md5));
                String url = md5UrlMap.get(md5);
                String cover = url.substring(0, url.lastIndexOf("."));
                delUrls.add(cover + "_.jpg");
                System.out.println("删除文件：" + url);
                System.out.println("删除封面：" + cover + "_.jpg");
            }
        }
        rabbitTemplate.convertAndSend("delete-queue", StringUtils.join(delUrls, ","));
        spaceManagementService.updateSpace(uid, -size);
        return Result.success();
    }

    @Override
    public Result upload(Long uid, Long fid, Long pid, MultipartFile file, String md5, Integer index, Integer total) {
        long size = file.getSize();
        if (size <= 0){
            return Result.error(StatusCodeEnum.FILE_UPLOAD_FAILED);
        }
        if (uid == null || file.isEmpty()){
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        RLock lock = redissonClient.getLock("upload_" + uid + ":" + pid + ":" + fid + ":" + index);
        try{
            lock.tryLock(2,2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return Result.error(StatusCodeEnum.SEND_FREQUENTLY);
        }
        if (pid.compareTo(FileConstants.ROOT_DIR_ID) != 0){
            FileInfo cacheParent = cacheService.getFile(uid, pid);
            if (cacheParent == null){
                cacheParent = fileInfoMapper.getFileById(pid, uid);
                if (cacheParent == null){
                    cacheService.saveNullFile(uid, pid);
                    return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
                }
                cacheService.saveFile(cacheParent);
            }
            if (cacheParent.getId() == null){
                return Result.error(StatusCodeEnum.DIRECTORY_NOT_EXISTS);
            }
        }
        File tempDir = null;
        DefaultTransactionDefinition td = new DefaultTransactionDefinition();
        TransactionStatus status = dataSourceTransactionManager.getTransaction(td);
        if (index == 0) {
            List<FileInfo> fileByMd5s = fileInfoMapper.getFileByMd5(md5);
            if (fileByMd5s != null && !fileByMd5s.isEmpty()) {
                FileInfo fileByMd5 = fileByMd5s.get(0);
                Space space = spaceManagementService.getUsedSpace(uid);
                if (fileByMd5.getSize() + space.getUseSpace() > space.getTotalSpace()) {
                    log.error("秒传失败，空间不足");
                    return Result.error(StatusCodeEnum.SPACE_NOT_ENOUGH);
                }
                fileByMd5.setId(IdUtil.getSnowflake(1, 1).nextId());
                fileByMd5.setPid(pid);
                fileByMd5.setUid(uid);
                fileByMd5.setFileName(file.getOriginalFilename());
                fileByMd5.setStatus(FileConstants.STATUS_USE);
                fileByMd5.setUpdateTime(LocalDateTime.now());
                fileByMd5.setCreateTime(LocalDateTime.now());
                try {
                    saveFile(fileByMd5);
                    dataSourceTransactionManager.commit(status);
                    cacheService.deleteUploadFile(uid, fid);
                    log.info("秒传");
                    return Result.success(100);
                } catch (Exception e) {
                    log.error("秒传失败");
                    dataSourceTransactionManager.rollback(status);
                    return Result.error(StatusCodeEnum.FILE_UPLOAD_FAILED);
                }
            }
        }

        // 0 ~ n - 1分片处理
        Space usedSpace = spaceManagementService.getUsedSpace(uid);
        cacheService.updateUploadFileSize(uid, fid, size);
        long uploadFileSize = cacheService.getUploadFileSize(uid, fid);
        try {
            if (usedSpace.getUseSpace() + uploadFileSize > usedSpace.getTotalSpace()) {
                log.error("空间不足");
                throw new Exception("空间不足");
            }
            tempDir = new File(tempPath + fid);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            File tempFileIndex = new File(tempDir, index + "");
            file.transferTo(tempFileIndex);
            if (index == total - 1) { // 上传完成
                FileInfo dbFile = new FileInfo();
                long dbId = IdUtil.getSnowflake(1, 1).nextId();
                dbFile.setId(dbId);
                dbFile.setFileName(file.getOriginalFilename());
                dbFile.setType(FileType.getFileType(file.getContentType()).getType());
                dbFile.setPid(pid);
                dbFile.setUid(uid);
                dbFile.setStatus(FileConstants.STATUS_USE);
                dbFile.setMd5(md5);
                dbFile.setTransformed(FileConstants.TRANSFORMED_PROCESS);
                dbFile.setUpdateTime(LocalDateTime.now());
                dbFile.setCreateTime(LocalDateTime.now());
                dbFile.setSize(uploadFileSize);
                saveFile(dbFile);
                dataSourceTransactionManager.commit(status);
                cacheService.deleteUploadFile(uid, fid);
                String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                String contentType = file.getContentType();
                // 使用rabbitmq 异步转码
                rabbitTemplate.convertAndSend("upload-queue",new UploadMQType(uid, dbId, fid, contentType, suffix, tempPath, md5));
                return Result.success(200);
            }
            dataSourceTransactionManager.commit(status);
            return Result.success(200);
        }catch (Exception e){
            dataSourceTransactionManager.rollback(status);
            if (tempDir != null){
                FileUtils.deleteQuietly(tempDir);
            }
            cacheService.deleteUploadFile(uid, fid);
            log.error("文件上传失败");
            return Result.error(StatusCodeEnum.FILE_UPLOAD_FAILED);
        }
    }


    private void saveFile(FileInfo saveFile) throws Exception {
        RLock lock = redissonClient.getLock("fileInfoLock" + saveFile.getUid() + ":" + saveFile.getPid() + ":" + saveFile.getFileName());
        boolean b = false;
        try {
            b = lock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!b){
                throw new Exception("文件锁失败");
            }
            // 查询是否存在同名文件
            if (cacheService.ParentKeyCodeExists(saveFile.getUid(), saveFile.getPid())){
                if (cacheService.getChildByFileName(saveFile.getUid(), saveFile.getPid(), saveFile.getFileName())){
                    saveFile.setFileName(saveFile.getFileName() + "("+ RandomUtil.randomString(2) +")");
                }
            }else {
                FileInfo fileChildName = fileInfoMapper.getFileChildName(saveFile.getPid(), saveFile.getUid(), saveFile.getFileName());
                if (fileChildName != null){
                    saveFile.setFileName(saveFile.getFileName() + "("+ RandomUtil.randomString(2) +")");
                    cacheService.saveFile(fileChildName);
                }
            }
            int insert = fileInfoMapper.insert(saveFile);
            if (insert > 0) {
                pathAddSize(saveFile.getUid(), saveFile.getPid(), saveFile.getSize());
                cacheService.addChildren(saveFile.getUid(), saveFile.getPid(), saveFile);
                cacheService.saveFile(saveFile);
                spaceManagementService.updateSpace(saveFile.getUid(), saveFile.getSize());
                return;
            }
            throw new Exception("文件保存失败");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            if (b){
                lock.unlock();
            }
        }
    }

    @Override
    public Result restoreFile(List<Long> ids, Long uid) {
        if (ids == null || uid == null){
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        if (ids.isEmpty()){
            return Result.success();
        }
        List<FileInfo> dbFiles = fileInfoMapper.getFileByIds(ids, uid, FileConstants.STATUS_RECYCLE);
        if (dbFiles == null || dbFiles.isEmpty()){
            return Result.success();
        }
        Set<Long> set = new HashSet<>();
        Map<Long, Long> pidAndSize = new HashMap<>();
        for (FileInfo fileInfo : dbFiles){
            FileInfo parentFile = cacheService.getFile(uid, fileInfo.getPid());
            if (parentFile == null && parentFile.getId() != null){
                parentFile = fileInfoMapper.getFileById(fileInfo.getPid(), uid);
                if (parentFile == null){
                    // 父目录不存在, 无法还原,设置父目录为根目录
                    fileInfo.setPid(FileConstants.ROOT_DIR_ID);
                }else {
                    fileInfo.setPid(parentFile.getId());
                }
            }
            // 判断父目录下是否存在同名文件
            if (cacheService.ParentKeyCodeExists(uid, fileInfo.getPid())){
                if(cacheService.getChildByFileName(uid, fileInfo.getPid(), fileInfo.getFileName())){
                    fileInfo.setFileName(fileInfo.getFileName() + "("+ RandomUtil.randomString(2) +")");

                }
            }else {
                FileInfo fileChildName = fileInfoMapper.getFileChildName(fileInfo.getPid(), uid, fileInfo.getFileName());
                if (fileChildName != null){
                    fileInfo.setFileName(fileInfo.getFileName() + "("+ RandomUtil.randomString(2) +")");
                }
            }
            // 还原目录和子文件
            Stack<FileInfo> stk = new Stack<>();
            stk.add(fileInfo);
            while (!stk.isEmpty()){
                FileInfo fileSTK = stk.pop();
                set.add(fileSTK.getId());
                cacheService.deleteFile(uid, fileSTK.getId());
                if (fileSTK.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
                    List<FileInfo> childFiles = fileInfoMapper.getChildFiles(fileSTK.getId(), uid, FileConstants.STATUS_DELETE);
                    stk.addAll(childFiles);
                }
            }
            fileInfo.setStatus(FileConstants.STATUS_USE);
            fileInfo.setUpdateTime(LocalDateTime.now());
            cacheService.addChildren(uid, fileInfo.getPid(), fileInfo);
            cacheService.saveFile(fileInfo);
            if (pidAndSize.containsKey(fileInfo.getPid())){
                pidAndSize.put(fileInfo.getPid(), pidAndSize.get(fileInfo.getPid()) + fileInfo.getSize());
            }else {
                pidAndSize.put(fileInfo.getPid(), fileInfo.getSize());
            }
        }
        // 更新父目录大小
        for (Long pid : pidAndSize.keySet()){
            pathAddSize(uid, pid, pidAndSize.get(pid));
        }

        List<Long> updateIds = set.stream().toList();
        if (updateIds.isEmpty()){
            return Result.success();
        }
        fileInfoMapper.updateStatusBatch(uid, updateIds, FileConstants.STATUS_USE, LocalDateTime.now());
        return Result.success();
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
