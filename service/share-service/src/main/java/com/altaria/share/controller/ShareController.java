package com.altaria.share.controller;


import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.vo.FileInfoVO;
import com.altaria.common.pojos.share.entity.Share;
import com.altaria.common.pojos.share.vo.ShareVO;
import com.altaria.share.service.ShareService;
import jakarta.servlet.http.HttpServletResponse;
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
    public Result<Share> creatShareLink(@RequestHeader(UserConstants.USER_ID) Long userId,
                                          @RequestBody Share share){
        return shareService.createShareLink(userId, share);
    }

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

    //  取消分享
    @DeleteMapping("/cancel/{shareIds}")
    public Result cancelShare(@PathVariable("shareIds") List<Long> shareIds,
                              @RequestHeader(UserConstants.USER_ID) Long userId){
        return shareService.cancelShare(shareIds, userId);
    }

    //  获取分享链接信息
    @GetMapping("/info/{shareId}")
    public Result<ShareVO> getShareInfo(@PathVariable("shareId") Long shareId){
        Share share = shareService.getShareById(shareId);
        ShareVO shareVO = BeanUtil.copyProperties(share, ShareVO.class);
        return Result.success(shareVO);
    }



    // 获取分享链接信息（文件列表）
    @GetMapping("/list/{shareId}")
    public Result<List<FileInfoVO>> getShareInfo(@PathVariable("shareId") Long shareId,
                                                  @RequestParam(value = "path", required = false) Long path,
                                                  @RequestHeader(UserConstants.USER_ID) Long userId) {
        return shareService.getShareListInfo(shareId, path, userId);
    }




    // 下载分享文件（直接下载并统计下载次数）
    @GetMapping("/download/{shareId}/{fid}")
    public void downloadShareFile(@PathVariable("shareId") Long shareId,
                                  @PathVariable("fid") Long fid,
                                  HttpServletResponse response){
        shareService.downloadShareFile(shareId, fid, response);
    }



    // 预览分享文件
    @GetMapping("/preview/{shareId}/{fid}")
    public void previewShareFile(@PathVariable("shareId") Long shareId,
                                 @PathVariable("fid") Long fid,
                                 HttpServletResponse response){
        shareService.previewShareFile(shareId, fid, response);
    }

}