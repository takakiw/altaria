package com.altaria.share.controller;


import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.vo.FileInfoVO;
import com.altaria.common.pojos.share.entity.Share;
import com.altaria.common.pojos.share.vo.ShareVO;
import com.altaria.share.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/share")
public class ShareController {

    @Autowired
    private ShareService shareService;


    //  创建分享链接（文件）
    @PostMapping("/create")
    public Result<String> creatShareLink(@RequestHeader(UserConstants.USER_ID) Long userId,
                                         @RequestBody Share share){
        return shareService.createShareLink(userId, share);
    }

    // 获取分享链接信息（文件）
    @GetMapping("/{shareId}")
    public Result<List<FileInfoVO>> getShareInfo(@PathVariable("shareId") Long shareId) {
        Share share = shareService.getShareById(shareId);
        if (share == null) {
            return Result.error(StatusCodeEnum.SHARE_NOT_FOUND);
        }

        // 使用 FileServiceClient 获取文件信息
        List<FileInfoVO> fileInfoList = null;
        return Result.success(fileInfoList);
    }

    // todo: 验证分享链接


    //  获取分享链接信息列表
    @GetMapping("/urlList")
    public Result<List<ShareVO>> getShareListInfo(@RequestHeader(UserConstants.USER_ID) Long userId){
        if (userId == null){
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        List<Share> shareList = shareService.getShareList(userId);
        List<ShareVO> shareVOS = BeanUtil.copyToList(shareList, ShareVO.class);
        return Result.success(shareVOS);
    }


    // todo: 下载分享文件（直接下载并统计下载次数）

    // todo: 取消分享

    // todo: 预览分享文件

}