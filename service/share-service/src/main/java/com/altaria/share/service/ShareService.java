package com.altaria.share.service;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.share.entity.Share;

import java.util.List;

public interface ShareService {
    Result<String> createShareLink(Long userId, Share share);

    List<Share> getShareList(Long userId);

    Share getShareById(Long shareId);
}
