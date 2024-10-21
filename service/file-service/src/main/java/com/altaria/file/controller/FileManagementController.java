package com.altaria.file.controller;

import com.altaria.common.constants.FeignConstants;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;
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

    // 通过FeignClient调用, 获取文件信息
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

    @PostMapping("/saveShare")
    public Result saveFileToCloud(@RequestBody SaveShare saveShare){
        String requestPathService = request.getHeader(FeignConstants.REQUEST_ID_HEADER);
        // 判断是否从FeignClient调用
        if (requestPathService== null || !requestPathService.equals(FeignConstants.REQUEST_ID_VALUE)){
            return Result.error();
        }
        return fileManagementService.saveFileToCloud(saveShare.getFids(), saveShare.getShareUid(), saveShare.getPath(), saveShare.getUserId());
    }


    /**
     * 上传文件
     * @param uid
     * @param fid
     * @param pid
     * @param file
     * @param md5
     * @param index
     * @param total
     * @return
     */
    @PostMapping("/upload")
    public Result upload(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                         @NotNull Long fid,
                         @NotNull Long pid,
                         @NotNull MultipartFile file,
                         @NotNull String md5,
                         @NotNull Integer index,
                         @NotNull Integer total) {
        return fileManagementService.upload(uid,fid, pid, file, md5, index, total);
    }

    /**
     * 下载文件
     * @param response
     * @param id
     * @param uid
     */
    @GetMapping("/download/{id}")
    public void download(HttpServletResponse response,
                         @PathVariable("id") Long id,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        fileManagementService.download(response, id, uid);
    }

    /**
     * 创建文件夹
     * @param fileInfo
     * @return
     */
    @PostMapping("/mkdir")
    public Result mkdir(@RequestBody FileInfo fileInfo,
                        @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.mkdir(uid, fileInfo.getPid(), fileInfo.getFileName());
    }

    /**
     * 移动文件
     * @param fileInfo
     * @return
     */
    @PutMapping("/mvfile")
    public Result moveFile(@RequestBody FileInfo fileInfo,
                           @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.moveFile(fileInfo, uid);
    }

    /**
     * 重命名文件
     * @param fileInfo
     * @param uid
     * @return
     */
    @PutMapping("/rename")
    public Result rename(@RequestBody FileInfo fileInfo,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.renameFile(fileInfo, uid);
    }

    /**
     * 获取文件列表
     * @param id
     * @param type
     * @param fileName
     * @param order
     * @param uid
     * @return PageResult<FileInfo>
     */
    @GetMapping("/list/{id}")
    public Result<PageResult<FileInfo>> getChildrenList(@PathVariable("id") Long id,
                                            @RequestParam(value = "type", required = false) Integer type,
                                            @RequestParam(value = "fileName" , required = false) String fileName,
                                            @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                                            @RequestParam(value = "order", required = false, defaultValue = "0") Integer order) {
        return fileManagementService.getPagedFileList(id, uid, type,fileName, order);
    }

    /**
     * 获取文件路径
     * @param path
     * @param uid
     * @return Result
     */
    @GetMapping("/path")
    public Result<List<FileInfo>> getPath(@RequestParam(value = "path", required = true) Long path,
                          @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.getPath(path, uid);
    }


    /**
     * 删除文件
     * @param ids
     * @param uid
     * @return
     */
    @DeleteMapping("/del/{ids}")
    public Result delete(@PathVariable("ids") List<Long> ids,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.deleteFile(ids, uid);
    }


    /**
     *  彻底删除文件
     * @param ids
     * @param uid
     * @return
     */
    @DeleteMapping("/remove/{ids}")
    public Result remove(@PathVariable("ids") List<Long> ids,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.removeFile(ids, uid);
    }

    /**
     * 恢复文件
     * @param ids
     * @param uid
     * @return
     */
    @PutMapping("/restore/{ids}")
    public Result restore(@PathVariable("ids") List<Long> ids,
                          @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid) {
        return fileManagementService.restoreFile(ids, uid);
    }

    /**
     * 获取回收站文件列表
     * @param uid
     * @return
     */
    @GetMapping("/recycle/list")
    public Result<PageResult<FileInfo>> recycleList(@RequestHeader(value = UserConstants.USER_ID, required = false) Long uid){
        return fileManagementService.getRecycleFileList(uid);
    }
}
