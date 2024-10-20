package com.altaria.share.service;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.vo.FileInfoVO;
import com.altaria.common.pojos.share.entity.Share;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface ShareService {
    Result<Share> createShareLink(Long userId, Share share);

    List<Share> getShareList(Long userId);

    Share getShareById(Long shareId);

    Result cancelShare(List<Long> shareIds, Long userId);

    void downloadShareFile(Long shareId, Long fid, HttpServletResponse response);

    Result<List<FileInfoVO>> getShareListInfo(Long shareId, Long path, Long userId);

    void previewShareFile(Long shareId, Long fid, HttpServletResponse response);
}
