package com.altaria.user.controller;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.LoginUser;
import com.altaria.user.service.LoginService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/user/login")
@Validated
public class LoginController {

    @Autowired
    private LoginService loginService;

    /**
     *  获取验证码
     * @param email
     * @param type
     * @return
     */
    @GetMapping("/code")
    public Result getCode(@NotBlank String email, @RequestParam(required = false, defaultValue = UserConstants.TYPE_LOGIN) String type){
        return loginService.sendCode(email, type);
    }

    /**
     *  登录
     * @param loginUser
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody @Valid LoginUser loginUser) {
        return loginService.login(loginUser);
    }

    /**
     *  注册
     * @param loginUser
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody @Valid LoginUser loginUser) {
        return loginService.register(loginUser);
    }
}
