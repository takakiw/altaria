package com.altaria.share;

import com.altaria.share.mapper.ShareMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SQLTest {

    @Autowired
    private ShareMapper shareMapper;

    @Test
    public void test() {
        System.out.println(shareMapper.getShareById(1848384333458993152L));
    }

}
