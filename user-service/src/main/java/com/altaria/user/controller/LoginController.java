package com.altaria.user.controller;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.LoginUser;
import com.altaria.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/code")
    public Result getCode(String email, @RequestParam(required = false, defaultValue = UserConstants.TYPE_LOGIN) String type){
        return loginService.sendCode(email, type);
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginUser loginUser) {
        return loginService.login(loginUser);
    }

    @PostMapping("/register")
    public Result register(@RequestBody LoginUser loginUser) {
        return loginService.register(loginUser);
    }
}
