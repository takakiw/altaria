package com.altaria.user.controller;


import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.vo.UserVO;
import com.altaria.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/user/user")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * @param id 用户id
     * @param request 获取token中的用户id
     * @return
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable("id") Long id, HttpServletRequest request) {
        Object uId = request.getAttribute(UserConstants.USER_ID);
        if (uId == null && id == -1) {
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        return userService.getUserById(id == -1? Long.parseLong(uId.toString()) : id);
    }


    @PostMapping("/uploadAvatar")
    public Result uploadAvatar(MultipartFile file, HttpServletRequest request) {
        Object uId = request.getAttribute(UserConstants.USER_ID);
        if (uId == null) {
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        if (file == null || !file.getContentType().contains("image")){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        return userService.uploadAvatar(file, Long.parseLong(uId.toString()));
    }

    @GetMapping("/avatar/{avatar}")
    public void getAvatar(@PathVariable("avatar") String avatar, HttpServletResponse response) {
        userService.downloadAvatar(avatar, response);
    }


}
