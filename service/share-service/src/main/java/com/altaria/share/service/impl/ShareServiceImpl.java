package com.altaria.share.service.impl;


import cn.hutool.core.util.IdUtil;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.share.entity.Share;
import com.altaria.share.mapper.ShareMapper;
import com.altaria.share.service.ShareService;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class ShareServiceImpl implements ShareService {


    @Autowired
    private ShareMapper shareMapper;

    @Value("${file.share.url}")
    private String shareUrl;


    @Override
    public Result<String> createShareLink(Long userId, Share share) {
        if (userId == null){
            return Result.error(StatusCodeEnum.ILLEGAL_REQUEST);
        }
        Share dbShare = new Share();
        dbShare.setId(IdUtil.getSnowflake().nextId());
        dbShare.setFids(share.getFids());
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
            return Result.success(url);
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


}
