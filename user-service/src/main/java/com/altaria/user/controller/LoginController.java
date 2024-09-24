package com.altaria.user.controller;

import com.altaria.common.exception.BaseException;
import com.altaria.common.pojos.common.Result;
import com.altaria.user.service.UserService;
import com.altaria.common.pojos.user.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/code")
    public Result getCode(String email) throws BaseException {
        if (StringUtils.isEmpty(email)){
            System.out.println("邮箱不能为空");
            throw new BaseException("邮箱不能为空!");
        }
        return userService.sendCode(email);
    }

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        return userService.login(user);
    }
}
