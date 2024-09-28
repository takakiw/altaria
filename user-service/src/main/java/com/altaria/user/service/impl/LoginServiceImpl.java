package com.altaria.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.LoginUser;
import com.altaria.redis.UserRedisService;
import com.altaria.user.mapper.UserMapper;
import com.altaria.user.service.LoginService;
import com.altaria.common.pojos.user.entity.User;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.altaria.common.utils.JWTUtil.userToJWT;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRedisService userRedisService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public Result sendCode(String email, String type) {
        if (StringUtils.isEmpty(email) || !email.matches(UserConstants.EMAIL_REGEX)){
            log.info("邮箱参数错误");
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        if (!type.equals(UserConstants.TYPE_LOGIN) && !type.equals(UserConstants.TYPE_REGISTER)){
            log.info("邮件类型错误");
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
       threadPoolTaskExecutor.execute(() -> {
           String code = RandomStringUtils.random(6, true, true);
           userRedisService.saveEmailCode(type,code, email);
           String text = type.equals(UserConstants.TYPE_REGISTER)?UserConstants.EMAIL_REGISTER_TEXT:UserConstants.EMAIL_LOGIN_TEXT;
           String subject = type.equals(UserConstants.TYPE_REGISTER)?UserConstants.EMAIL_REGISTER_SUBJECT:UserConstants.EMAIL_LOGIN_SUBJECT;
           sendEmail(email,subject, String.format(text, code));
       });
        log.info("验证码发送成功");
        return Result.success();
    }

    @Override
    public Result register(LoginUser loginUser) {
        if (StringUtils.isAnyEmpty(loginUser.getUserName(),loginUser.getPassword(), loginUser.getEmail())){
            log.info("注册参数错误");
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        String code = userRedisService.getEmailCode(UserConstants.TYPE_REGISTER, loginUser.getEmail());

        if (StringUtils.isEmpty(code) || !code.equals(loginUser.getCode())) {
            return Result.error(StatusCodeEnum.VERIFY_CODE_ERROR);
        }
        User user = BeanUtil.copyProperties(loginUser, User.class);
        User dbUser = userMapper.getUserByEmail(user.getEmail());
        if (dbUser != null){
            log.info("邮箱已经被使用");
            return Result.error(StatusCodeEnum.EMAIL_ALREADY_EXIST);
        }
        dbUser = userMapper.getUserByUserName(user.getUserName());
        if (dbUser != null){
            log.info("用户名已经被使用");
            return Result.error(StatusCodeEnum.USER_ALREADY_EXIST);
        }
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        user.setAvatar(UserConstants.DEFAULT_AVATAR);
        user.setRole(UserConstants.DEFAULT_ROLE);
        user.setNickName(UserConstants.DEFAULT_NICKNAME+RandomStringUtils.random(5, true, true));
        int flag = userMapper.insert(user);
        if (flag > 0){
            log.error("用户注册失败 {}", flag);
            return Result.success();
        }
        log.info("用户注册成功 {}", flag);
        return Result.error(StatusCodeEnum.ERROR);
    }

    @Override
    public Result login(LoginUser loginUser) {
        User user = BeanUtil.copyProperties(loginUser, User.class);
        // 校验用户名或邮箱
        if (StringUtils.isAllBlank(user.getUserName(), user.getEmail())){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        // 使用用户名登录
        if (StringUtils.isNotEmpty(user.getUserName())){
            // MD5加密密码
            String md5pwd = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
            user.setPassword(md5pwd);
            User dbUser = userMapper.select(user);
            if (dbUser != null){
                log.info("用户:{} 登录成功", dbUser.getId());
                return Result.success(userToJWT(dbUser));
            }
            log.info("用户名或密码错误");
            return Result.error(StatusCodeEnum.USER_OR_PASSWORD_ERROR);
        }else { // 使用邮箱登录
            String code = userRedisService.getEmailCode(UserConstants.TYPE_LOGIN, user.getEmail());
            if (StringUtils.isEmpty(code) || !code.equals(loginUser.getCode())) {
                return Result.error(StatusCodeEnum.VERIFY_CODE_ERROR);
            }
            User dbUser = userMapper.select(user);
            if (dbUser == null){
                return Result.error(StatusCodeEnum.EMAIL_NOT_EXIST);
            }
            log.info("用户:{} 邮箱登录成功", dbUser.getId());
            return Result.success(userToJWT(dbUser));
        }
    }


    private void sendEmail(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            message.setFrom(new InternetAddress(from));
            message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setContent(content, "text/html;charset=UTF-8");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
        }
    }
}
