package com.altaria.feign.client;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.file.entity.FileInfo;

import com.altaria.feign.fallback.FileServiceClientFallbackFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(contextId = "fileServiceClient", value = "file-service", fallbackFactory = FileServiceClientFallbackFactory.class)
public interface FileServiceClient {

    @RequestMapping(value = "/file/upload-image",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,method = {RequestMethod.POST},produces = {MediaType.APPLICATION_JSON_UTF8_VALUE},headers = "content-type=multipart/form-data")
    String uploadImage(@RequestPart("file") MultipartFile file);

    @GetMapping("/file/info/{fids}")
    Result<List<FileInfo>> getFileInfos(@PathVariable("fids") List<Long> fids,
                                              @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid);

    @GetMapping("/file/path")
    Result<List<FileInfo>> getPath(@RequestParam(value = "path", required = true) Long path,
                                          @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid);

    @GetMapping("/list/{id}")
    public Result<PageResult<FileInfo>> getChildrenList(@PathVariable("id") Long id,
                                            @RequestParam(value = "type", required = false) Integer type,
                                            @RequestParam(value = "fileName" , required = false) String fileName,
                                            @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid,
                                            @RequestParam(value = "order", required = false, defaultValue = "0") Integer order);

    @GetMapping("/download/{id}")
    public void download(HttpServletResponse response,
                         @PathVariable("id") Long id,
                         @RequestHeader(value = UserConstants.USER_ID, required = false) Long uid);
}
