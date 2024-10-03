package com.altaria.user.controller;


import com.alibaba.fastjson.JSONObject;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.pojos.user.vo.UserVO;
import com.altaria.common.utils.BaseContext;
import com.altaria.redis.UserRedisService;
import com.altaria.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/user/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;



    /**
     * 获取用户信息
     * @param id 用户id
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable("id") @Valid @NotNull Long id) {
        Long uId = BaseContext.getCurrentId();
        if (uId == null && id == -1) {
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        return userService.getUserById(id == -1? uId : id);
    }


    /**
     * 上传头像
     * @param file
     * @return 上传结果
     */
    @PostMapping("/uploadAvatar")
    public Result uploadAvatar(@Valid @NotNull MultipartFile file) {
        Long uId = BaseContext.getCurrentId();
        if (uId == null) {
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        if (!file.getContentType().contains("image")){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        return userService.uploadAvatar(file, uId);
    }

    /**
     * 下载头像
     * @param avatar
     * @param response
     */
    @GetMapping("/avatar/{avatar}")
    public void getAvatar(@PathVariable("avatar") String avatar, HttpServletResponse response) {
        userService.downloadAvatar(avatar, response);
    }

    /**
     * 更新用户信息
     * @param user
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result updateUser(@RequestBody @Valid User user) {
        return userService.updateUser(user);
    }

    /**
     * 发送邮箱验证码
     * @param email
     * @return 发送结果
     */
    @GetMapping("/code")
    public Result sndEmailCode(@Valid @NotNull String email) {
        return userService.sendEmailCode(email);
    }
}
