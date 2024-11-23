package com.altaria.feign.client;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;

import com.altaria.common.pojos.file.entity.SaveShare;
import com.altaria.feign.fallback.FileServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(contextId = "fileServiceClient", value = "file-service", fallbackFactory = FileServiceClientFallbackFactory.class, path = "/file")
public interface FileServiceClient {

    @RequestMapping(value = "/upload-image",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,method = {RequestMethod.POST},produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},headers = "content-type=multipart/form-data")
    String uploadImage(@RequestPart("file") MultipartFile file);

    @GetMapping("/info/{fids}")
    Result<List<FileInfo>> getFileInfos(@PathVariable("fids") List<Long> fids,
                                               @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid);

    @GetMapping("/path")
    Result<List<FileInfo>> getPath(@RequestParam(value = "path", required = false) Long path,
                                          @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid);

    @GetMapping("/list")
    Result<PageResult<FileInfo>> getChildrenList(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "fileName" , required = false) String fileName,
            @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
            @RequestParam(value = "order", required = false, defaultValue = "0") Integer order);

    @PostMapping("/file/saveShare")
    Result saveFileToCloud(@RequestBody SaveShare saveShare);

    @GetMapping("/download/sign/{id}")
    Result<String> downloadSign(@PathVariable("id") Long id,
                                       @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid);


    @GetMapping("/api/info/{id}")
    Result<FileInfo> getFileInfo(@PathVariable("id") Long id,
                                        @RequestParam(value = "uid", required = false) Long uid);
}
