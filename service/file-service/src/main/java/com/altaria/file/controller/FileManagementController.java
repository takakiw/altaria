package com.altaria.file.controller;

import com.altaria.common.constants.FeignConstants;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.common.pojos.file.entity.MoveFile;
import com.altaria.common.pojos.file.entity.SaveShare;
import com.altaria.file.service.FileManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileManagementController {


    @Autowired
    private HttpServletRequest request;


    @Autowired
    private FileManagementService fileManagementService;

    // 通过FeignClient调用, 批量获取文件信息
    @GetMapping("/info/{fids}")
    public Result<List<FileInfo>> getFileInfos(@PathVariable("fids") List<Long> fids,
                                               @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid){
        String requestPathService = request.getHeader(FeignConstants.REQUEST_ID_HEADER);
        // 判断是否从FeignClient调用
        if (requestPathService== null || !requestPathService.equals(FeignConstants.REQUEST_ID_VALUE)){
            return Result.error();
        }
        return fileManagementService.getFileInfoBatch(fids, uid);
    }
    // 通过FeignClient调用, 判断获取单个文件
    @GetMapping("/api/info/{id}")
    public Result<FileInfo> getFileInfo(@PathVariable("id") Long id,
                                        @RequestParam(value = "uid", required = false) Long uid){
        String requestPathService = request.getHeader(FeignConstants.REQUEST_ID_HEADER);
        // 判断是否从FeignClient调用
        if (requestPathService== null || !requestPathService.equals(FeignConstants.REQUEST_ID_VALUE)){
            return Result.error();
        }
        return fileManagementService.getFileInfo(id, uid);
    }

    // 通过接口调用上传图片，内部使用FeignClient进行调用
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadImage(@RequestPart("file") MultipartFile file){
        String requestPathService = request.getHeader(FeignConstants.REQUEST_ID_HEADER);
        // 判断是否从FeignClient调用
        if (requestPathService== null || !requestPathService.equals(FeignConstants.REQUEST_ID_VALUE)){
            return null;
        }
        return fileManagementService.uploadImage(file);
    }

    // 通过接口调用保存分享文件，内部使用FeignClient进行调用
    @PostMapping("/file/saveShare")
    public Result saveFileToCloud(@RequestBody SaveShare saveShare){
        String requestPathService = request.getHeader(FeignConstants.REQUEST_ID_HEADER);
        // 判断是否从FeignClient调用
        if (requestPathService== null || !requestPathService.equals(FeignConstants.REQUEST_ID_VALUE)){
            return Result.error();
        }
        return fileManagementService.saveFileToCloud(saveShare.getFids(), saveShare.getShareUid(), saveShare.getPath(), saveShare.getUserId());
    }


    @PostMapping("/upload")
    public Result upload(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                         @NotNull Long fid,
                         @NotNull Long pid,
                         @NotNull MultipartFile file,
                         @NotNull String fileName,
                         @NotNull String type,
                         @NotNull String md5,
                         @NotNull Integer index,
                         @NotNull Integer total) {
        return fileManagementService.upload(uid,fid, pid, file, fileName, type, md5, index, total);
    }


    @GetMapping("/download/sign/{id}")
    public Result<String> downloadSign(@PathVariable("id") Long id,
                                       @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.downloadSign(id, uid);
    }


    @GetMapping("/download/{url}")
    public void download(HttpServletResponse response,
                         @PathVariable("url") String url,
                         @RequestParam("uid") Long uid,
                         @RequestParam("expire") Long expire,
                         @RequestParam("sign") String sign) {
        fileManagementService.download(response, url, uid, expire, sign);
    }


    @PostMapping("/mkdir")
    public Result mkdir(@RequestBody FileInfo fileInfo,
                        @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.mkdir(uid, fileInfo.getPid(), fileInfo.getFileName());
    }


    @PutMapping("/mvfile")
    public Result moveFile(@RequestBody MoveFile moveFile,
                           @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.moveFile(moveFile, uid);
    }


    @PutMapping("/rename")
    public Result rename(@RequestBody FileInfo fileInfo,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.renameFile(fileInfo, uid);
    }


    @GetMapping("/list")
    public Result<PageResult<FileInfo>> getChildrenList(
                                            @RequestParam(value = "id", required = false) Long id,
                                            @RequestParam(value = "type", required = false) Integer type,
                                            @RequestParam(value = "fileName" , required = false) String fileName,
                                            @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                                            @RequestParam(value = "order", required = false, defaultValue = "0") Integer order) {
        return fileManagementService.getPagedFileList(id, uid, type,fileName, order);
    }



    @GetMapping("/path")
    public Result<List<FileInfo>> getPath(@RequestParam(value = "path", required = false) Long path,
                          @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.getPath(path, uid);
    }



    @DeleteMapping("/del/{ids}")
    public Result delete(@PathVariable("ids") List<Long> ids,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.deleteFile(ids, uid);
    }



    @DeleteMapping("/remove/{ids}")
    public Result remove(@PathVariable("ids") List<Long> ids,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.removeFile(ids, uid);
    }


    @PutMapping("/restore/{ids}")
    public Result restore(@PathVariable("ids") List<Long> ids,
                          @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.restoreFile(ids, uid);
    }


    @GetMapping("/recycle/list")
    public Result<PageResult<FileInfo>> recycleList(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid){
        return fileManagementService.getRecycleFileList(uid);
    }


    @DeleteMapping("/delUpload/{id}")
    public Result delUpload(@PathVariable("id") Long id,
                            @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.delUpload(id, uid);
    }
}
