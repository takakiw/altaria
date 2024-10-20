package com.altaria.share.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.altaria.common.annotation.CheckCookie;
import com.altaria.common.constants.ShareConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.common.pojos.file.vo.FileInfoVO;
import com.altaria.common.pojos.share.entity.Share;
import com.altaria.feign.client.FileServiceClient;
import com.altaria.share.mapper.ShareMapper;
import com.altaria.share.service.ShareService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShareServiceImpl implements ShareService {


    @Autowired
    private ShareMapper shareMapper;

    @Value("${file.share.url}")
    private String shareUrl;

    @Autowired
    private FileServiceClient fileServiceClient;


    @Override
    public Result<Share> createShareLink(Long userId, Share share) {
        if (userId == null){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        Result<List<FileInfo>> result = fileServiceClient.getFileInfos(share.getFids(), userId);
        if (result.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        List<FileInfo> fileInfos = result.getData();
        if (fileInfos == null || fileInfos.size() == 0){
            return Result.error(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        if (share.getType() == ShareConstants.TYPE_ONE_FILE){
            if (fileInfos.size() > 1){
                return Result.error(StatusCodeEnum.SHARE_TYPE_ERROR);
            }
        }
        List<Long> dbFids = fileInfos.stream().map(FileInfo::getId).toList();
        String name = fileInfos.get(0).getFileName() + "等" + fileInfos.size() + "个文件";
        Share dbShare = new Share();
        dbShare.setId(IdUtil.getSnowflake().nextId());
        dbShare.setFids(dbFids);
        dbShare.setName(name);
        dbShare.setUid(userId);
        dbShare.setExpire(share.getExpire());
        String url = shareUrl + "/" + dbShare.getId() + "/" + dbShare.getSign();
        dbShare.setUrl(url);
        if(StringUtils.isNotBlank(share.getSign()) && share.getSign().length() == 4){
            dbShare.setSign(share.getSign());
        }else{
            dbShare.setSign(RandomStringUtils.random(4, "0123456789"));
        }
        int insert = shareMapper.insert(dbShare);
        if (insert > 0) {
            return Result.success(dbShare);
        }
        return Result.error(StatusCodeEnum.CREATE_SHARE_LINK_ERROR);
    }

    @Override
    public List<Share> getShareList(Long userId) {
        Share share = new Share();
        share.setUid(userId);
        return shareMapper.select(share);
    }

    @Override
    public Share getShareById(Long shareId) {
        return shareMapper.getShareById(shareId);
    }

    @Override
    public Result cancelShare(List<Long> shareIds, Long userId) {
        if (shareIds == null || shareIds.size() == 0 || userId == null){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        shareMapper.deleteByIds(shareIds, userId);
        return Result.success();
    }

    @CheckCookie
    @Override
    public void downloadShareFile(Long shareId, Long fid, HttpServletResponse response) {
        Share share = shareMapper.getShareById(shareId);
        if (share == null){
            response.setStatus(404);
        }
        Result<List<FileInfo>> path = fileServiceClient.getPath(fid, share.getUid());
        if (path.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            response.setStatus(404);
        }
        List<FileInfo> data = path.getData();
        if (data == null || data.size() == 0){
            response.setStatus(404);
        }
        List<FileInfo> fileInfos = data.stream().filter(f -> share.getFids().contains(f.getId())).toList();
        if (fileInfos.size() == 0){
            response.setStatus(404);
        }
        fileServiceClient.download(response, fid, share.getUid());
    }

    @CheckCookie
    @Override
    public Result<List<FileInfoVO>> getShareListInfo(Long shareId, Long path) {
        Share shareById = shareMapper.getShareById(shareId);
        if (shareById == null){
            return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
        }
        if (path == null){ // 根目录
            Result<List<FileInfo>> fileInfo = fileServiceClient.getFileInfos(shareById.getFids(), shareById.getUid());
            if (fileInfo.getCode() != StatusCodeEnum.SUCCESS.getCode()){
                return Result.error(StatusCodeEnum.ERROR);
            }
            List<FileInfo> data = fileInfo.getData();
            if (data == null || data.size() == 0){
                shareMapper.deleteByIds(List.of(shareId), shareById.getUid());
                return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
            }
            List<FileInfoVO> fileInfoVOList = data.stream().map(f -> BeanUtil.copyProperties(f, FileInfoVO.class)).toList();
            return Result.success(fileInfoVOList);
        }else { // 非根目录
            // 判断根目录到当前目录是否存在分享文件中
            if(!checkShareFile(shareById, path)){
                return Result.error(StatusCodeEnum.SHARE_NOT_EXISTS);
            }
            // 该目录被分享，返回该目录下的文件信息
            Result<PageResult<FileInfo>> childrenList = fileServiceClient.getChildrenList(path, null, null, shareById.getId(), 0);
            if (childrenList.getCode() != StatusCodeEnum.SUCCESS.getCode()){
                return Result.error(StatusCodeEnum.ERROR);
            }
            PageResult<FileInfo> pageResult = childrenList.getData();
            return Result.success(pageResult.getRecords().stream().map(f -> BeanUtil.copyProperties(f, FileInfoVO.class)).toList());
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

    @CheckCookie
    @Override
    public void previewShareFile(Long shareId, Long fid, HttpServletResponse response) {
        Share share = shareMapper.getShareById(shareId);
        if (share == null){
            response.setStatus(404);
        }
        Result<List<FileInfo>> path = fileServiceClient.getPath(fid, share.getUid());
        if (path.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            response.setStatus(404);
        }
        List<FileInfo> data = path.getData();
        if (data == null || data.size() == 0){
            response.setStatus(404);
        }
        List<FileInfo> fileInfos = data.stream().filter(f -> share.getFids().contains(f.getId())).toList();
        if (fileInfos.size() == 0){
            response.setStatus(404);
        }

    }

    @Override
    public Result<List<FileInfoVO>> getSharePath(Long shareId, Long path) {
        Share share = shareMapper.getShareById(shareId);
        if (share == null){
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
            sharePath.add(BeanUtil.copyProperties(fileInfo, FileInfoVO.class));
            if (share.getFids().contains(fileInfo.getId())){
                break;
            }
        }
        return Result.success(sharePath);
    }
}
