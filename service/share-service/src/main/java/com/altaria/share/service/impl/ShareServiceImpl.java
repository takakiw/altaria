package com.altaria.share.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
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
import com.altaria.share.cache.ShareCacheService;
import com.altaria.share.mapper.ShareMapper;
import com.altaria.share.service.ShareService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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



    @Override
    public Result<Share> createShareLink(Long userId, Share share) {
        if (userId == null){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
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
        String url = shareUrl + dbShare.getId();
        dbShare.setType(ShareConstants.TYPE_FILE); // 暂时只支持文件分享， 后续支持笔记分享
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
    public List<Share> getShareList(Long userId, Integer category) {
        if (Boolean.TRUE.equals(cacheService.KeyExists(userId))){
            List<Share> userAllShare = cacheService.getUserAllShare(userId);
            if (userAllShare == null || userAllShare.size() == 0){
                return new ArrayList<>();
            }
            List<Share> shares = userAllShare.stream().filter(share -> share != null && share.getUid() != null && Objects.equals(share.getType(), category) && share.getExpire().isAfter(LocalDateTime.now())).toList();
            List<Share> expiredShare = userAllShare.stream().filter(share -> share.getExpire().isBefore(LocalDateTime.now())).toList();
            if (expiredShare.size() > 0){
                CompletableFuture.supplyAsync(() -> { // 异步删除过期分享
                    shareMapper.deleteByIds(expiredShare.stream().map(Share::getId).toList(), userId);
                    cacheService.deleteShareBatch(expiredShare);
                    return null;
                });
            }
            return shares;
        }else{
            Share share = new Share();
            share.setUid(userId);
            List<Share> shareList = shareMapper.select(share);
            if (shareList == null || shareList.size() == 0){
                cacheService.saveUserNullChild(userId);
                return new ArrayList<>();
            }
            cacheService.saveUserAllShare(userId, shareList);
            shareList = shareList.stream().filter(s -> Objects.equals(s.getType(), category)).toList();
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
        if(share.getUid() == null){
            return null;
        }
        if (share.getExpire().isBefore(LocalDateTime.now())){
            shareMapper.deleteByIds(List.of(shareId), share.getUid());
            cacheService.deleteShareBatch(List.of(share));
            return null;
        }
        return share;
    }

    @Override
    public Result cancelShare(List<Long> shareIds, Long userId) {
        if (shareIds == null || shareIds.size() == 0 || userId == null){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        // 查询数据库中是否存在这些分享
        List<Share> dbShares = shareMapper.getShareByIdBatch(userId, shareIds);
        List<Long> realIds = dbShares.stream().map(Share::getId).toList();
        if (realIds.size() == 0){
            return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
        }
        shareMapper.deleteByIds(realIds, userId);
        cacheService.deleteShareBatch(dbShares);
        return Result.success();
    }

    @CheckCookie
    @Override
    public Result<String> downloadShareFile(Long shareId, Long fid) {
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
        // 获取文件路径
        Result<List<FileInfo>> path = fileServiceClient.getPath(fid, shareInfo.getUid());
        if (path.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            return Result.error();
        }
        List<FileInfo> data = path.getData();
        if (data == null || data.size() == 0){
            return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        // 判断文件是否在分享列表中
        Share finalShareInfo = shareInfo;
        List<FileInfo> fileInfos = data.stream().filter(f -> finalShareInfo.getFids().contains(f.getId())).toList();
        if (fileInfos.size() == 0){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        // 获取最后一个文件信息，就是fid对应的文件
        FileInfo fileInfo = data.get(data.size() - 1);
        // 如果是目录，则返回404
        if (fileInfo.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        // 增加下载次数
        shareMapper.incrementVisit(shareId);
        // 下载文件
        return fileServiceClient.downloadSign(fileInfo.getId(), shareInfo.getUid());
    }


    @CheckCookie
    @Override
    public Result<String> previewShareFile(Long shareId, Long fid, String category) {
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
        // 获取文件路径
        Result<List<FileInfo>> path = fileServiceClient.getPath(fid, shareInfo.getUid());
        if (path.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            return Result.error();
        }
        List<FileInfo> data = path.getData();
        if (data == null || data.size() == 0){
            return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        // 判断文件是否在分享列表中
        Share finalShareInfo = shareInfo;
        List<FileInfo> fileInfos = data.stream().filter(f -> finalShareInfo.getFids().contains(f.getId())).toList();
        if (fileInfos.size() == 0){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        // 获取最后一个文件信息， 就是fid对应的文件
        FileInfo fileInfo = data.get(data.size() - 1);
        // 如果是目录，则返回404
        if (fileInfo.getType().compareTo(FileType.DIRECTORY.getType()) == 0){
            return Result.error(StatusCodeEnum.FILE_CANNOT_PREVIEW);
        }
        return fileServiceClient.sign(fileInfo.getId(), shareInfo.getUid(), category);
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
                    return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
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
        if(path == null){
            return Result.success(new ArrayList<>());
        }
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
        if (share.getType() != ShareConstants.TYPE_FILE){
            return Result.error(StatusCodeEnum.ONLY_FILE_SAVED); // 暂时只支持文件分享
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

    @Autowired
    private HttpServletRequest request;

    @Override
    public void verifyShareSign(Long shareId, String sign, HttpServletResponse response) {
        if (shareId == null){
            writeResponse(response, JSONObject.toJSONString(Result.error(StatusCodeEnum.ILLEGAL_REQUEST)));
            return;
        }
        if (sign == null){
            boolean flag = false;
            // 从cookie中获取sign
            try{
                Cookie[] cookies = request.getCookies();
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(ShareConstants.COOKIE_NAME + shareId)){
                        sign = cookie.getValue();
                        flag = true;
                        System.out.println("验证成功: " + sign);
                        break;
                    }
                }
                if (!flag){
                    writeResponse(response, JSONObject.toJSONString(Result.error(StatusCodeEnum.SHARE_SIGN_ERROR)));
                    return;
                }
            }catch (Exception e){
                writeResponse(response, JSONObject.toJSONString(Result.error(StatusCodeEnum.SHARE_SIGN_ERROR)));
                return;
            }
        }
        Share share = cacheService.getShareInfo(shareId);
        if (share == null){
            share = shareMapper.getShareById(shareId);
            if (share == null){
                cacheService.saveNullShareInfo(shareId);
                writeResponse(response, JSONObject.toJSONString(Result.error(StatusCodeEnum.SHARE_NOT_EXISTS)));
                return;
            }
            cacheService.saveShareInfo(share);
        }
        if (share.getUid() == null || share.getExpire().isBefore(LocalDateTime.now())){
            writeResponse(response, JSONObject.toJSONString(Result.error(StatusCodeEnum.SHARE_NOT_EXISTS)));
            return;
        }
        if (share.getSign().compareTo(sign) != 0){
            writeResponse(response, JSONObject.toJSONString(Result.error(StatusCodeEnum.SHARE_SIGN_ERROR)));
            return;
        }
        writeResponse(response, JSONObject.toJSONString(Result.success()));
        Cookie cookie = new Cookie(ShareConstants.COOKIE_NAME + shareId, sign);
        cookie.setMaxAge(60 * 60 * 6); // 设置过期时间为6小时
        cookie.setPath("/"); // 设置cookie的作用范围
        cookie.setHttpOnly(true); // 设置cookie不可通过js访问
        response.addCookie(cookie);
        // 设置前端可以访问到cookie的内容
        response.setHeader("Access-Control-Expose-Headers", "Set-Cookie"); // 注意这是 "Set-Cookie"
        return ;
    }

    public void writeResponse(HttpServletResponse response, String content) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(content);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
