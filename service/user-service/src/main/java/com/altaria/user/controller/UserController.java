package com.altaria.user.controller;


import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.pojos.user.vo.UserVO;
import com.altaria.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;



    /**
     * 获取用户信息
     * @param id 用户id
     * @return 用户信息
     */
    @GetMapping("/info/{id}")
    public Result<UserVO> getUserById(@PathVariable("id") @NotNull Long id,
                                      @RequestHeader(value = UserConstants.USER_ID, required = false) Long uId) {
        return userService.getUserById(id, uId);
    }


    /**
     * 上传头像
     * @param file
     * @return 上传结果
     */
    @PostMapping("/uploadAvatar")
    public Result uploadAvatar(@NotNull MultipartFile file,
                               @RequestHeader(value = UserConstants.USER_ID, required = false) Long uId) {
        return userService.uploadAvatar(file, uId);
    }


    /**
     * 更新用户信息
     * @param user
     * @return 更新结果
     */
    @PutMapping("/update")
    public Result updateUser(@RequestBody @Valid User user,
                            @RequestHeader(value = UserConstants.USER_ID, required = false) Long uId) {
        return userService.updateUser(user, uId);
    }

    /**
     * 发送邮箱验证码
     * @param email
     * @return 发送结果
     */
    @GetMapping("/code")
    public Result sndEmailCode(@NotBlank String email) {
        return userService.sendEmailCode(email);
    }
}
