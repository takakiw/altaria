package com.altaria.file.listener;

import cn.hutool.crypto.digest.DigestUtil;
import com.altaria.common.constants.FileConstants;
import com.altaria.common.constants.MinioConstants;
import com.altaria.common.enums.FileType;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.file.service.FileManagementService;
import com.altaria.rabbitmq.config.entity.mq.RecycleMqType;
import com.altaria.rabbitmq.config.entity.mq.UploadMQType;
import com.altaria.common.utils.FfmpegUtil;
import com.altaria.config.exception.BaseException;
import com.altaria.file.cache.FileCacheService;
import com.altaria.file.mapper.FileInfoMapper;
import com.altaria.minio.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@Slf4j
@Component
public class FileStoreHandle {

    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;

    @Autowired
    private MinioService minioService;

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private FileManagementService fileManagementService;

    @Autowired
    private FileCacheService cacheService;

    @RabbitListener(queues = "delete-avatar-queue")
    public void deleteAvatar(String message) {
        minioService.deleteFile(message, MinioConstants.AVATAR_BUCKET_NAME);
    }

    @RabbitListener(queues = "delete-queue")
    public void deleteFile(String message) {
        List<String> list = Arrays.stream(message.split(",")).toList();
        minioService.deleteFile(list);
    }

    @RabbitListener(queues = "recycle-delete-queue")
    public void deleteRecycleFile(RecycleMqType recycle) {
        fileManagementService.removeRecycleFile(recycle.getFids());
    }


    @RabbitListener(queues = "upload-queue")
    public void uploadFile(UploadMQType message) {
        Long uid = message.getUid();
        Long dbId = message.getDbId();
        Long fid = message.getFid();
        String contentType = message.getContentType();
        String suffix = message.getSuffix();
        String tempPath = message.getTempPath();
        String md5 = message.getMd5();

        DefaultTransactionDefinition td = new DefaultTransactionDefinition();
        TransactionStatus status = dataSourceTransactionManager.getTransaction(td);

        // 转码
        try {
            transferFile(uid, dbId, fid, contentType, suffix, tempPath, md5);
            dataSourceTransactionManager.commit(status);
        } catch (Exception e) {
            dataSourceTransactionManager.rollback(status);
            log.error("转码失败, 文件id: {},用户id: {}, 错误信息: {}", dbId, uid, e.getMessage());
        }
    }

    private void transferFile(Long uid, long dbId, Long fid, String contentType, String suffix, String tempPath, String md5) {
        // 判断MD5是否重复
        List<FileInfo> fileByMd5 = fileInfoMapper.getFileByMd5(md5);
        if (fileByMd5!= null && fileByMd5.size() > 1){
            log.info("文件MD5重复, 文件id: {},用户id: {}", dbId, uid);
            // 删除临时文件， 直接使用已有文件
            File sourceFile = new File(tempPath + fid);
            if (sourceFile.exists()){
                try {
                    FileUtils.deleteDirectory(sourceFile);
                    log.info("删除临时文件成功 {}", sourceFile.getAbsolutePath());
                } catch (IOException e) {
                    throw new BaseException(e.getMessage());
                }
            }
            // 使用已有文件更新数据库
            FileInfo fileInfo = fileByMd5.get(0);
            fileInfoMapper.updateURLAndCoverByMd5(fileInfo.getUrl(), fileInfo.getCover(), fileInfo.getMd5(), FileConstants.TRANSFORMED_END);
            // 更新缓存中文件信息，包括url和封面，以及转码状态
            cacheService.updateFileCoverAndUrl(uid, dbId,  fileInfo.getCover(),fileInfo.getUrl());
            cacheService.updateFileTransformed(uid, dbId, FileConstants.TRANSFORMED_END);
            return;
        }
        File sourceDir = new File(tempPath + fid);
        File[] files = sourceDir.listFiles();
        if (files == null || files.length == 0){
            return;
        }
        String fileDataPath = tempPath + "union\\" + dbId + suffix;
        String fileCoverPath = tempPath + "cover\\" + dbId + "_.jpg";
        File targetFile = new File(fileDataPath);
        RandomAccessFile writeFile = null;
        File data = new File(fileDataPath);
        File cover = new File(fileCoverPath);
        String minioId = UUID.randomUUID().toString().replace("-", "");
        try {
            writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024  *10];
            for(int i = 0; i < files.length; i++){
                int len = -1;
                File chunkFile = new File(sourceDir,i + "");
                try (RandomAccessFile readFile = new RandomAccessFile(chunkFile, "r")) {
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并分片失败");
                    throw new RuntimeException("合并分片失败");
                }
            }
            writeFile.close();
            // 判断文件完整性
            String localMd5 = DigestUtil.md5Hex(data);
            if (!StringUtils.equals(md5, localMd5)){
                log.error("文件MD5校验失败, 文件id: {},用户id: {}", dbId, uid);
                fileInfoMapper.updateURLAndCoverByMd5(null, null, md5, FileConstants.TRANSFORMED_ERROR);
                cacheService.updateFileTransformed(uid, dbId, FileConstants.TRANSFORMED_ERROR);
                return;
            }
            // 完整合并完成在data中, 开始转码
            Integer type = FileType.getFileType(contentType).getType();
            try {
                if (type.compareTo(FileType.VIDEO.getType()) == 0){ // 转码视频
                    // 提取封面
                    FfmpegUtil.createTargetThumbnail(data, FileConstants.THUMBNAIL_WIDTH, cover);
                    // 上传minio
                    // 上传文件
                    minioService.upLoadFile(minioId + suffix, new FileInputStream(data), contentType);
                    // 上传封面
                    minioService.upLoadFile(minioId + "_.jpg", new FileInputStream(cover), "image/jpeg");
                    // 更新数据库
                    int i = fileInfoMapper.updateURLAndCoverByMd5(minioId + suffix, minioId + "_.jpg", md5, FileConstants.TRANSFORMED_END);
                    if (i <= 0){
                        log.error("数据库更新失败, 文件id: {},用户id: {}", dbId, uid);
                        throw new BaseException("数据库更新失败");
                    }

                } else if (type.compareTo(FileType.IMAGE.getType()) == 0) { // 压缩图片
                    FfmpegUtil.compressImage(data, FileConstants.IMAGE_WIDTH,cover, false); // 压缩图片
                    // 上传minio
                    // 上传文件
                    minioService.upLoadFile(minioId + suffix, new FileInputStream(data), contentType);
                    // 上传封面
                    minioService.upLoadFile(minioId + "_.jpg", new FileInputStream(cover), "image/jpeg");
                    // 更新数据库
                    int i = fileInfoMapper.updateURLAndCoverByMd5(minioId + suffix, minioId + "_.jpg", md5, FileConstants.TRANSFORMED_END);
                    if (i <= 0){
                        log.error("数据库更新失败, 文件id: {},用户id: {}", dbId, uid);
                        throw new BaseException("数据库更新失败");
                    }
                }else { // 其他文件
                    // 上传minio
                    // 上传文件
                    minioService.upLoadFile(minioId + suffix, new FileInputStream(data), contentType);
                    // 更新数据库
                    int i = fileInfoMapper.updateURLAndCoverByMd5(minioId + suffix, null, md5, FileConstants.TRANSFORMED_END);
                    if (i <= 0){
                        log.error("数据库更新失败, 文件id: {},用户id: {}", dbId, uid);
                        throw new BaseException("数据库更新失败");
                    }
                }
                // 更新缓存中文件信息，包括url和封面，以及转码状态
                cacheService.updateFileCoverAndUrl(uid, dbId,  minioId + "_.jpg",minioId + suffix);
                cacheService.updateFileTransformed(uid, dbId, FileConstants.TRANSFORMED_END);
            }catch (Exception e){
                minioService.deleteFile(minioId + suffix);
                minioService.deleteFile(minioId + "_.jpg");
                fileInfoMapper.updateURLAndCoverByMd5(null, null, md5, FileConstants.TRANSFORMED_ERROR);
                cacheService.updateFileTransformed(uid, dbId, FileConstants.TRANSFORMED_ERROR);
                log.error("转码失败, 文件id: {},用户id: {}, 错误信息: {}", dbId, uid, e.getMessage());
                throw new BaseException("转码失败");
            }
        }catch (Exception e){
            log.error("转码失败, 文件id: {},用户id: {}, 错误信息: {}", dbId, uid, e.getMessage());
            throw new BaseException("转码失败");
        }finally {
            if (null != writeFile) {
                try {
                    writeFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sourceDir.exists()) {
                try {
                    FileUtils.deleteDirectory(sourceDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (data.exists()){
                 data.delete();
            }
            if (cover.exists()){
                cover.delete();
            }
        }
    }
}
