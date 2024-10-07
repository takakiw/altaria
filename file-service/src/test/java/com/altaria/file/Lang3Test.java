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
import java.util.UUID;

@SpringBootTest
public class Lang3Test {


    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Autowired
    private MinioService minioService;

    @Test
    public void test() throws IOException {

        File file = new File("D:\\CodeStore\\GitTest\\easypan-main\\webser\\web_app\\easypan\\file\\202404\\3494146119kePANfVadk.mp4");
        FileInputStream inputStream = new FileInputStream(file);
        String fileName = UUID.randomUUID().toString().replace("-", "") + ".mp4";
        minioService.upLoadFile(fileName, inputStream, "video/mp4");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setId(IdUtil.getSnowflake(1, 1).nextId());
        fileInfo.setUid(1L);
        fileInfo.setUrl(fileName);
        fileInfo.setFileName("test.mp4");
        fileInfo.setType(FileType.VIDEO.getType());
        fileInfo.setSize(file.length());
        fileInfo.setMd5(DigestUtil.md5Hex(file));
        fileInfo.setPid(0L);
        int insert = fileInfoMapper.insert(fileInfo);
        System.out.println(insert);
    }
}
