package com.altaria.share.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.altaria.common.annotation.CheckCookie;
import com.altaria.common.constants.ShareConstants;
import com.altaria.common.enums.FileType;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.common.pojos.file.entity.SaveShare;
import com.altaria.common.pojos.file.vo.FileInfoVO;
import com.altaria.common.pojos.share.entity.Share;
import com.altaria.feign.client.FileServiceClient;
import com.altaria.minio.service.MinioService;
import com.altaria.share.cache.ShareCacheService;
import com.altaria.share.mapper.ShareMapper;
import com.altaria.share.service.ShareService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ShareServiceImpl implements ShareService {


    @Autowired
    private ShareMapper shareMapper;

    @Value("${file.share.url}")
    private String shareUrl;

    @Autowired
    private FileServiceClient fileServiceClient;

    @Autowired
    private ShareCacheService cacheService;

    @Autowired
    private MinioService minioService;


    @Override
    public Result<Share> createShareLink(Long userId, Share share) {
        if (userId == null){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        // 判断分享类型
        if (share.getType() == ShareConstants.TYPE_ONE_FILE){
            if (share.getFids().size() > 1){
                return Result.error(StatusCodeEnum.SHARE_TYPE_ERROR);
            }
        }
        // 获取分享的文件信息
        Result<List<FileInfo>> result = fileServiceClient.getFileInfos(share.getFids(), userId);
        if (result.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        List<FileInfo> fileInfos = result.getData();
        if (fileInfos == null || fileInfos.size() == 0){
            return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        List<Long> dbFids = fileInfos.stream().map(FileInfo::getId).toList();
        String name = fileInfos.get(0).getFileName() + "等" + fileInfos.size() + "个文件";
        Share dbShare = new Share();
        dbShare.setId(IdUtil.getSnowflake().nextId());
        dbShare.setFids(dbFids);
        dbShare.setName(name);
        dbShare.setUid(userId);
        dbShare.setExpire(share.getExpire());
        String url = shareUrl + "/share/" + dbShare.getId();
        dbShare.setType(share.getType());
        dbShare.setUrl(url);
        if(StringUtils.isNotBlank(share.getSign()) && share.getSign().length() == 4){
            dbShare.setSign(share.getSign());
        }else{
            dbShare.setSign(RandomStringUtils.random(4, "0123456789"));
        }
        int insert = shareMapper.insert(dbShare);
        if (insert > 0) {
            dbShare.setVisit(0L);
            dbShare.setCreateTime(LocalDateTime.now());
            cacheService.saveShareInfo(dbShare);
            return Result.success(dbShare);
        }
        return Result.error(StatusCodeEnum.CREATE_SHARE_LINK_ERROR);
    }

    @Override
    public List<Share> getShareList(Long userId) {
        if (cacheService.KeyExists(userId)){
            List<Share> userAllShare = cacheService.getUserAllShare(userId);
            List<Share> shares = userAllShare.stream().filter(share -> share.getExpire().isAfter(LocalDateTime.now())).toList();
            cacheService.deleteShareBatch(userAllShare.stream().filter(share -> share.getExpire().isBefore(LocalDateTime.now())).toList());
            return shares;
        }else{
            Share share = new Share();
            share.setUid(userId);
            List<Share> shareList = shareMapper.select(share);
            cacheService.saveUserAllShare(userId, shareList);
            return shareList.stream().filter(s -> s.getExpire().isAfter(LocalDateTime.now())).toList();
        }
    }

    @Override
    public Share getShareById(Long shareId) {
        Share share = cacheService.getShareInfo(shareId);
        if (share == null){
            share = shareMapper.getShareById(shareId);
            if (share == null){
                cacheService.saveNullShareInfo(shareId);
                return null;
            }
            cacheService.saveShareInfo(share);
        }
        if (share.getUid() == null || share.getExpire().isBefore(LocalDateTime.now())){
            shareMapper.deleteByIds(List.of(shareId), share.getUid());
        }
        return shareMapper.getShareById(shareId);
    }

    @Override
    public Result cancelShare(List<Long> shareIds, Long userId) {
        if (shareIds == null || shareIds.size() == 0 || userId == null){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        // 查询数据库中是否存在这些分享
        List<Share> dbShares = shareMapper.getShareByIdBatch(userId, shareIds);
        List<Long> realIds = dbShares.stream().map(Share::getId).toList();
        shareMapper.deleteByIds(realIds, userId);
        if (cacheService.KeyExists(userId)) {
            cacheService.deleteUserShare(userId, realIds);
        }
        cacheService.deleteShareBatch(dbShares);
        return Result.success();
    }

    @CheckCookie
    @Override
    public void downloadShareFile(Long shareId, Long fid, HttpServletResponse response) {
        // 获取分享信息
        Share shareInfo = cacheService.getShareInfo(shareId);
        if (shareInfo == null){
            shareInfo = shareMapper.getShareById(shareId);
            if (shareInfo == null){
                cacheService.saveNullShareInfo(shareId);
                response.setStatus(404);
                return;
            }
            cacheService.saveShareInfo(shareInfo);
        }
        if (shareInfo.getUid() == null || shareInfo.getExpire().isBefore(LocalDateTime.now())){
            response.setStatus(404);
            return;
        }
        // 获取文件路径
        Result<List<FileInfo>> path = fileServiceClient.getPath(fid, shareInfo.getUid());
        if (path.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            response.setStatus(404);
            return;
        }
        List<FileInfo> data = path.getData();
        if (data == null || data.size() == 0){
            response.setStatus(404);
            return;
        }
        // 判断文件是否在分享列表中
        Share finalShareInfo = shareInfo;
        List<FileInfo> fileInfos = data.stream().filter(f -> finalShareInfo.getFids().contains(f.getId())).toList();
        if (fileInfos.size() == 0){
            response.setStatus(404);
            return;
        }
        // 获取最后一个文件信息，就是fid对应的文件
        FileInfo fileInfo = data.get(data.size() - 1);
        // 如果是目录，则返回404
        if (fileInfo.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
            response.setStatus(404);
            return;
        }
        minioService.downloadFile(fileInfo.getUrl(), response);
    }

    @Autowired
    private HttpServletRequest request;

    @CheckCookie
    @Override
    public void previewShareFile(Long shareId, Long fid, HttpServletResponse response) {
        // 获取分享信息
        Share shareInfo = cacheService.getShareInfo(shareId);
        if (shareInfo == null){
            shareInfo = shareMapper.getShareById(shareId);
            if (shareInfo == null){
                cacheService.saveNullShareInfo(shareId);
                response.setStatus(404);
                return;
            }
            cacheService.saveShareInfo(shareInfo);
        }
        if (shareInfo.getUid() == null || shareInfo.getExpire().isBefore(LocalDateTime.now())){
            response.setStatus(404);
            return;
        }
        // 获取文件路径
        Result<List<FileInfo>> path = fileServiceClient.getPath(fid, shareInfo.getUid());
        if (path.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            response.setStatus(404);
            return;
        }
        List<FileInfo> data = path.getData();
        if (data == null || data.size() == 0){
            response.setStatus(404);
            return;
        }
        // 判断文件是否在分享列表中
        Share finalShareInfo = shareInfo;
        List<FileInfo> fileInfos = data.stream().filter(f -> finalShareInfo.getFids().contains(f.getId())).toList();
        if (fileInfos.size() == 0){
            response.setStatus(404);
            return;
        }
        // 获取最后一个文件信息， 就是fid对应的文件
        FileInfo fileInfo = data.get(data.size() - 1);
        // 如果是目录，则返回404
        if (fileInfo.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
            response.setStatus(404);
            return;
        }
        if (fileInfo.getType().compareTo(FileType.VIDEO.getType()) == 0
        || fileInfo.getType().compareTo(FileType.AUDIO.getType()) == 0){
            String range = request.getHeader("Range");
            range = range == null ? "bytes=0-" : range;
            String[] split = range.replace("bytes=", "").split("-");
            long start = Long.parseLong(split[0]);
            long end = split.length > 1 ? Long.parseLong(split[1]) : start + 1024 * 1024 - 1;
            minioService.previewVideo(fileInfo.getUrl(), response, start, end);
        }else {
            minioService.preview(fileInfo.getUrl(), response);
        }
    }

    @CheckCookie
    @Override
    public Result<List<FileInfoVO>> getShareListInfo(Long shareId, Long path) {
        // 获取分享信息
        Share shareInfo = cacheService.getShareInfo(shareId);
        if (shareInfo == null){
            shareInfo = shareMapper.getShareById(shareId);
            if (shareInfo == null){
                cacheService.saveNullShareInfo(shareId);
                return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
            }
            cacheService.saveShareInfo(shareInfo);
        }
        if (shareInfo.getUid() == null || shareInfo.getExpire().isBefore(LocalDateTime.now())){
            return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
        }
        if (path == null){ // 根目录
            Result<List<FileInfo>> fileInfo = fileServiceClient.getFileInfos(shareInfo.getFids(), shareInfo.getUid());
            if (fileInfo.getCode() != StatusCodeEnum.SUCCESS.getCode()){
                return Result.error(fileInfo.getMsg());
            }
            List<FileInfo> data = fileInfo.getData();
            if (data == null || data.size() == 0){
                shareMapper.deleteByIds(List.of(shareId), shareInfo.getUid());
                cacheService.deleteShareBatch(List.of(shareInfo));
                cacheService.deleteUserShare(shareInfo.getUid(), List.of(shareId));
                return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
            }
            List<FileInfoVO> fileInfoVOList = data.stream().map(f -> BeanUtil.copyProperties(f, FileInfoVO.class)).toList();
            return Result.success(fileInfoVOList);
        }else { // 非根目录
            // 创建两个异步任务
            Share finalShareInfo = shareInfo;
            CompletableFuture<Boolean> isShareFileFuture = CompletableFuture.supplyAsync(() ->
                    checkShareFile(finalShareInfo, path)
            );

            CompletableFuture<Result<PageResult<FileInfo>>> childrenListFuture = CompletableFuture.supplyAsync(() ->
                    fileServiceClient.getChildrenList(path, null, null, finalShareInfo.getUid(), 0)
            );

            // 等待两个任务完成，然后根据检查的结果决定下一步
            try {
                Boolean aBoolean = isShareFileFuture.get();
                if (!aBoolean){
                    return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
                }
                Result<PageResult<FileInfo>> childrenList = childrenListFuture.get();
                if (childrenList.getCode() != StatusCodeEnum.SUCCESS.getCode()){
                    return Result.error(childrenList.getMsg());
                }
                PageResult<FileInfo> data = childrenList.getData();
                return Result.success(data.getRecords().stream().map(f -> BeanUtil.copyProperties(f, FileInfoVO.class)).toList());
            } catch (Exception e) {
                return Result.error(StatusCodeEnum.ERROR);
            }
        }
    }

    private boolean checkShareFile(Share shareById, Long path) {
        Result<List<FileInfo>> pathResult = fileServiceClient.getPath(path, shareById.getUid());
        if (pathResult.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            return false;
        }
        List<FileInfo> data = pathResult.getData();
        if (data == null || data.size() == 0){
            return false;
        }
        List<FileInfo> fileInfos = data.stream().filter(f -> shareById.getFids().contains(f.getId())).toList();
        if (fileInfos.size() == 0){
            return false;
        }
        return true;
    }

    @Override
    public Result<List<FileInfoVO>> getSharePath(Long shareId, Long path) {
        // 获取分享信息
        Share share = cacheService.getShareInfo(shareId);
        if (share == null){
            share = shareMapper.getShareById(shareId);
            if (share == null){
                cacheService.saveNullShareInfo(shareId);
                return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
                }
            cacheService.saveShareInfo(share);
        }
        if (share.getUid() == null || share.getExpire().isBefore(LocalDateTime.now())){
            return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
        }
        Result<List<FileInfo>> pathResult = fileServiceClient.getPath(path, share.getUid());
        if (pathResult.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            return Result.error(StatusCodeEnum.ERROR);
        }
        List<FileInfo> data = pathResult.getData();
        if (data == null || data.size() == 0){
            return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
        }
        List<FileInfoVO> sharePath = new ArrayList<>();
        for (int i = data.size() - 1; i >= 0; i--) {
            FileInfo fileInfo = data.get(i);
            sharePath.add(0,BeanUtil.copyProperties(fileInfo, FileInfoVO.class));
            if (share.getFids().contains(fileInfo.getId())){
                break;
            }
        }
        return Result.success(sharePath);
    }

    @Override
    public Result saveToMyCloud(Long shareId, List<Long> fids, Long userId, Long path) {
        if (userId == null || fids == null || fids.size() == 0 || shareId == null){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        if (userId.compareTo(shareId) == 0){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        // 保存share中的fids等文件到我的云盘
        Share share = cacheService.getShareInfo(shareId);
        if (share == null){
            share = shareMapper.getShareById(shareId);
            if (share == null){
                cacheService.saveNullShareInfo(shareId);
                return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
            }
            cacheService.saveShareInfo(share);
        }
        if (share.getUid() == null || share.getExpire().isBefore(LocalDateTime.now())){
            return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
        }
        // 获取分享目录的路径
        Result<List<FileInfo>> resultPath = fileServiceClient.getPath(fids.get(0), share.getUid());
        if (resultPath.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            return Result.error(StatusCodeEnum.ERROR);
        }
        List<FileInfo> data = resultPath.getData();
        if (data == null || data.size() == 0){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        // 判断分享目录是否在分享列表中
        Share finalShare = share;
        List<FileInfo> isShareList = data.stream().filter(f -> finalShare.getFids().contains(f.getId())).toList();
        if (isShareList.size() == 0){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        // 把share.getUid用户的分享目录下的fids文件保存到我的云盘path目录下
        Result saveResult = fileServiceClient.saveFileToCloud(new SaveShare(fids, finalShare.getUid(), path, userId));
        if (saveResult.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            return Result.error(saveResult.getMsg());
        }
        return Result.success();
    }
}
