package com.altaria.file.controller;


import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
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
     * 获取文件签名
     * @param id
     * @param uid
     * @return
    */
    @GetMapping("/sign/{id}")
    public Result<String> sign(@PathVariable("id") Long id,
                               @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                               @RequestParam(value = "category", defaultValue = "file", required = false) String category) {
        return filePreviewService.sign(id, uid, category);
    }


    /**
     *  preview文件
     * @param response
     * @param id
     * @param uid
     */
    @GetMapping("/file/{id}")
    public void preview(HttpServletResponse response,
                        @PathVariable("id") Long id,
                        @RequestParam(value = "uid", required = false) Long uid,
                        @RequestParam(value = "expire", required = false) Long expire,
                        @RequestParam(value = "sign", required = false) String sign) {
        filePreviewService.preview(response, id, uid, expire, sign);
    }

    /**
     * preview视频
     * @param response
     * @param request
     * @param id
     * @param sign
     */
    @GetMapping("/video/{id}")
    public void video(HttpServletResponse response,
                      HttpServletRequest request,
                      @PathVariable("id") Long id,
                      @RequestParam("uid") Long uid,
                      @RequestParam("expire") Long expire,
                      @RequestParam("sign") String sign) {
        filePreviewService.previewVideo(request,response, id, uid, expire, sign);
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
                      @RequestParam(value = "uid", required = false) Long uid,
                      @RequestParam(value = "expire", required = false) Long expire,
                      @RequestParam(value = "sign", required = false) String sign) {
        filePreviewService.previewCover(response, id, uid, expire, sign);
    }
}
