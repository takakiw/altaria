package com.altaria.file.controller;

import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;

import com.altaria.common.pojos.file.entity.Space;
import com.altaria.common.pojos.file.vo.SpaceVO;
import com.altaria.file.service.FileInfoService;
import com.altaria.file.service.SpaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@RestController
@RequestMapping("/file")
@Validated
public class FileInfoController {

    @Autowired
    private FileInfoService fileInfoService;

    @Autowired
    private SpaceService spaceService;

    /**
     * 上传·
     * @param file
     * @param md5
     * @param index
     * @param total
     * @return
     */
    @PostMapping("/file/upload")
    public Result upload(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                         @NotNull Long pid,
                         @NotNull MultipartFile file,
                         @NotNull String md5,
                         @NotNull Integer index,
                         @NotNull Integer total) {
        return fileInfoService.upload(uid, pid, file, md5, index, total);
    }

    /**
     * 创建文件夹
     * @param fileInfo
     * @return
     */
    @PostMapping("/file/mkdir")
    public Result mkdir(@RequestBody FileInfo fileInfo,
                        @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileInfoService.mkdir(uid, fileInfo.getPid(), fileInfo.getFileName());
    }

    /**
     * 移动文件
     * @param fileInfo
     * @return
     */
    @PutMapping("file/mvfile")
    public Result moveFile(@RequestBody FileInfo fileInfo,
                           @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
       return fileInfoService.moveFile(fileInfo, uid);
    }

    /**
     * 重命名文件
     * @param fileInfo
     * @param uid
     * @return
     */
    @PutMapping("/file/rename")
    public Result rename(@RequestBody FileInfo fileInfo,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileInfoService.renameFile(fileInfo, uid);
    }

    /**
     * 获取文件列表
     * @param id
     * @param type
     * @param status
     * @param page
     * @param count
     * @param uid
     * @return
     */
    @GetMapping("/file/{id}")
    public Result<PageResult<FileInfo>> get(@PathVariable("id") Long id,
                                            @RequestParam(value = "type", required = false) Integer type,
                                            @RequestParam(value = "fileName" , required = false) String fileName,
                                            @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                                            @RequestParam(value = "order", required = false, defaultValue = "0") Integer order) {
        return fileInfoService.getPagedFileList(id, uid, type,fileName, order);
    }

    /**
     * 获取文件路径
     * @param path
     * @param uid
     * @return
     */
    @GetMapping("/file/path")
    public Result getPath(@RequestParam(value = "path", required = true) Long path,
                          @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileInfoService.getPath(path, uid);
    }

    /**
     *  preview文件
     * @param response
     * @param id
     * @param uid
     */
    @GetMapping("/file/preview/{id}")
    public void preview(HttpServletResponse response,
                        @PathVariable("id") Long id,
                        @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        fileInfoService.preview(response, id, uid);
    }

    /**
     * preview视频
     * @param response
     * @param request
     * @param id
     * @param uid
     */
    @GetMapping("/file/video/{id}")
    public void video(HttpServletResponse response,
                      HttpServletRequest request,
                      @PathVariable("id") Long id,
                      @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        fileInfoService.previewVideo(request,response, id, uid);
    }

    /**
     * 下载文件
     * @param response
     * @param id
     * @param uid
     */
    @GetMapping("/file/download/{id}")
    public void download(HttpServletResponse response,
                        @PathVariable("id") Long id,
                        @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        fileInfoService.download(response, id, uid);
    }

    @DeleteMapping("/file/{ids}")
    public Result delete(@PathVariable("ids") List<Long> ids,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileInfoService.deleteFile(ids, uid);
    }

    @DeleteMapping("/file/remove/{ids}")
    public Result remove(@PathVariable("ids") List<Long> ids,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileInfoService.removeFile(ids, uid);
    }

    @PutMapping("/file/restore/{ids}")
    public Result restore(@PathVariable("ids") List<Long> ids,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileInfoService.restoreFile(ids, uid);
    }


    @GetMapping("/space/space")
    public Result<SpaceVO> space(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return spaceService.getUsedSpace(uid);
    }

    @PutMapping("/space/update")
    public Result updateSpace(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                                       @RequestBody Space space) {
        return spaceService.updateSpace(uid, space.getUseSpace());
    }

}