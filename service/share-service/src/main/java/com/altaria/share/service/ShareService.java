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

    Result<String> downloadShareFile(Long shareId, Long fid);

    Result<List<FileInfoVO>> getShareListInfo(Long shareId, Long path);

    Result<String> previewShareFile(Long shareId, Long fid, String category);

    Result<List<FileInfoVO>> getSharePath(Long shareId, Long path);

    Result saveToMyCloud(Long shareId, List<Long> fids, Long userId, Long path);


    void verifyShareSign(Long shareId, String sign, HttpServletResponse response);
}
