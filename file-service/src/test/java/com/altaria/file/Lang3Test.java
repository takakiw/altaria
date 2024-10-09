package com.altaria.file;



import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.altaria.common.enums.FileType;
import com.altaria.common.pojos.file.entity.FileInfo;
import com.altaria.file.mapper.FileInfoMapper;
import com.altaria.minio.service.MinioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class Lang3Test {


    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private MinioService minioService;

    @Test
    public void test() throws IOException {

        File file = new File("C:\\Users\\小文与\\Pictures\\20240713150547.jpg");
        FileInputStream inputStream = new FileInputStream(file);
        String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
        minioService.upLoadFile(fileName, inputStream, "image/jpeg");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setId(IdUtil.getSnowflake(1, 1).nextId());
        fileInfo.setUid(1L);
        fileInfo.setUrl(fileName);
        fileInfo.setFileName("20240713150547.jpg");
        fileInfo.setType(FileType.getFileType("image/jpeg").getType());
        fileInfo.setSize(file.length());
        fileInfo.setMd5(DigestUtil.md5Hex(file));
        fileInfo.setPid(0L);
        int insert = fileInfoMapper.insert(fileInfo);
        System.out.println(insert);
    }

    @Test
    public void test2() throws Exception {
        List<String> urls = new ArrayList<>();
        urls.add("20240713150547.jpg");
        //urls.add("20240713150547.jpg");
        urls.add("开发.png");
        minioService.deleteFile(urls);
        minioService.deleteFile("20240713150547.jpg");
        Thread.sleep(10000);
    }

    @Test
    public void test3() throws Exception {
        System.out.println(FileType.getFileType("text/plain").getType());
    }
}
