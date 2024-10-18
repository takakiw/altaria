package com.altaria.file.controller;


import com.altaria.common.constants.UserConstants;
import com.altaria.file.service.FilePreviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/preview")
public class FilePreviewController {

    @Autowired
    private FilePreviewService filePreviewService;


    /**
     *  preview文件
     * @param response
     * @param id
     * @param uid
     */
    @GetMapping("/file/{id}")
    public void preview(HttpServletResponse response,
                        @PathVariable("id") Long id,
                        @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        filePreviewService.preview(response, id, uid);
    }

    /**
     * preview视频
     * @param response
     * @param request
     * @param id
     * @param uid
     */
    @GetMapping("/video/{id}")
    public void video(HttpServletResponse response,
                      HttpServletRequest request,
                      @PathVariable("id") Long id,
                      @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        filePreviewService.previewVideo(request,response, id, uid);
    }

    /**
     * preview封面
     * @param response
     * @param id
     * @param uid
     */
    @GetMapping("/cover/{id}")
    public void cover(HttpServletResponse response,
                      @PathVariable("id") Long id,
                      @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        filePreviewService.previewCover(response, id, uid);
    }
}
