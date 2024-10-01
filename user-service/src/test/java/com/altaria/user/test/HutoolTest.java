package com.altaria.user.test;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HutoolTest {

    @Autowired
    private UserMapper userMapper;
    @Test
    public void test() {
        User user = new User();
        user.setUserName("takaki");
        System.out.println(JSONObject.toJSONString(userMapper.select(user)));
        System.out.println(JSONObject.toJSONString(userMapper.getUserById(1841074506819571712L)));


        User user1 = new User();
        user1.setAvatar("https://www.baidu.com");
        user1.setId(1L);
        userMapper.updateUser(user1);
    }



}
