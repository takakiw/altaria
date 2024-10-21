package com.altaria.share;

import com.altaria.feign.client.FileServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
public class FileClientTest {

    @Autowired
    private FileServiceClient fileServiceClient;

    @Test
    public void test() {
        System.out.println(fileServiceClient.getPath(1L, 1L));
        System.out.println(fileServiceClient.getFileInfos(Arrays.asList(1L, 2L), 1L));
    }

}
