package com.altaria.user.controller;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.LoginUser;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.pojos.user.vo.LoginUserVO;
import com.altaria.user.cache.UserCacheService;
import com.altaria.user.service.LoginService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.altaria.common.utils.JWTUtil.userToJWT;


@RestController
@RequestMapping("/user/login")
@Validated
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserCacheService cacheService;

    /**
     *  获取验证码
     * @param email
     * @param type
     * @return
     */
    @GetMapping("/code")
    public Result<String> getCode(@NotBlank String email,
                                  @RequestParam(required = false, defaultValue = UserConstants.TYPE_LOGIN) String type){
        if (!email.matches(UserConstants.EMAIL_REGEX)
                || (!type.equals(UserConstants.TYPE_LOGIN) && !type.equals(UserConstants.TYPE_REGISTER))){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        Long emailCodeTTL = cacheService.getEmailCodeTTL(type, email);
        if (emailCodeTTL != null && emailCodeTTL.intValue() - 60 > 0){
            return Result.error(StatusCodeEnum.SEND_FREQUENTLY);
        }
        String code = RandomStringUtils.random(6, true, true);
        cacheService.saveEmailCode(type,code, email);
        loginService.sendCode(email, type, code);
        return Result.success();
    }

    /**
     *  登录
     * @param loginUser
     * @return
     */
    @PostMapping("/login")
    public Result<LoginUserVO> login(@RequestBody @Valid LoginUser loginUser) {
        if (StringUtils.isAllBlank(loginUser.getUserName(), loginUser.getEmail())){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        if (StringUtils.isNotEmpty(loginUser.getUserName())){
            User dbUser = loginService.login(loginUser);
            if (dbUser != null){
                LoginUserVO userVO = new LoginUserVO(dbUser.getId(), userToJWT(dbUser));
                return Result.success(userVO);
            }
            return Result.error(StatusCodeEnum.USER_OR_PASSWORD_ERROR);
        }else {
            String code = cacheService.getEmailCode(UserConstants.TYPE_LOGIN, loginUser.getEmail());
            if (StringUtils.isEmpty(code) || !code.equals(loginUser.getCode())) {
                return Result.error(StatusCodeEnum.VERIFY_CODE_ERROR);
            }
            User dbUser = loginService.login(loginUser);
            if (dbUser == null){
                return Result.error(StatusCodeEnum.EMAIL_NOT_REGISTERED);
            }
            LoginUserVO userVO = new LoginUserVO(dbUser.getId(), userToJWT(dbUser));
            return Result.success(userVO);
        }
    }

    /**
     *  注册
     * @param loginUser
     * @return
     */
    @PostMapping("/register")
    public Result<LoginUserVO> register(@RequestBody @Valid LoginUser loginUser) {
        if (StringUtils.isAnyBlank(loginUser.getUserName(),loginUser.getPassword(), loginUser.getEmail())){
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        String code = cacheService.getEmailCode(UserConstants.TYPE_REGISTER, loginUser.getEmail());
        if (StringUtils.isEmpty(code) || !code.equals(loginUser.getCode())) {
            return Result.error(StatusCodeEnum.VERIFY_CODE_ERROR);
        }
        User dbUser = loginService.getUserByEmail(loginUser.getEmail());
        if (dbUser != null) {
            return Result.error(StatusCodeEnum.EMAIL_ALREADY_EXIST);
        }
        dbUser = loginService.getUserByUserName(loginUser.getUserName());
        if (dbUser != null) {
            return Result.error(StatusCodeEnum.USER_ALREADY_EXIST);
        }
        User register = loginService.register(loginUser);
        if (register == null){
            return Result.error(StatusCodeEnum.ERROR);
        }
        cacheService.saveUser(register);
        return Result.success(new LoginUserVO(register.getId(), userToJWT(register)));
    }
}
