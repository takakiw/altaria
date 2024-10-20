package com.altaria.share.service.impl;


import cn.hutool.core.util.IdUtil;
import com.altaria.common.annotation.CheckCookie;
import com.altaria.common.enums.StatusCodeEnum;
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

import java.time.LocalDateTime;
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

        Result<List<FileInfo>> result = fileServiceClient.getFileInfo(share.getFids(), userId);
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
        if (share.getExpire() != null && share.getExpire().isBefore(LocalDateTime.now())){
            response.setStatus(404);
        }
        Result<List<FileInfo>> path = fileServiceClient.getPath(fid, share.getUid());
        if (path.getCode() != StatusCodeEnum.SUCCESS.getCode()){
            response.setStatus(404);
        }
    }

    @CheckCookie
    @Override
    public Result<List<FileInfoVO>> getShareListInfo(Long shareId, Long path, Long userId) {
        return null;
    }

    @CheckCookie
    @Override
    public void previewShareFile(Long shareId, Long fid, HttpServletResponse response) {

    }
}
