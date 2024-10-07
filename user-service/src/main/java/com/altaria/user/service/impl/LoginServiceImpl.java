package com.altaria.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.LoginUser;
import com.altaria.common.pojos.user.vo.LoginUserVO;
import com.altaria.redis.RedisService;
import com.altaria.user.mapper.UserMapper;
import com.altaria.user.service.LoginService;
import com.altaria.common.pojos.user.entity.User;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;


import static com.altaria.common.utils.JWTUtil.userToJWT;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public Result sendCode(String email, String type) {
        if (!email.matches(UserConstants.EMAIL_REGEX)
                || (!type.equals(UserConstants.TYPE_LOGIN) && !type.equals(UserConstants.TYPE_REGISTER))){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        if (redisService.getEmailCodeTTL(type, email) - 60 > 0){
            return Result.error(StatusCodeEnum.SEND_FREQUENTLY);
        }
       threadPoolTaskExecutor.execute(() -> {
           String code = RandomStringUtils.random(6, true, true);
           redisService.saveEmailCode(type,code, email);
           String text = type.equals(UserConstants.TYPE_REGISTER)?UserConstants.EMAIL_REGISTER_TEXT:UserConstants.EMAIL_LOGIN_TEXT;
           String subject = type.equals(UserConstants.TYPE_REGISTER)?UserConstants.EMAIL_REGISTER_SUBJECT:UserConstants.EMAIL_LOGIN_SUBJECT;
           sendEmail(email,subject, String.format(text, code));
       });
        return Result.success();
    }

    @Override
    public Result register(LoginUser loginUser) {
        if (StringUtils.isAnyBlank(loginUser.getUserName(),loginUser.getPassword(), loginUser.getEmail())){
            return Result.error(StatusCodeEnum.PARAM_NOT_NULL);
        }
        String code = redisService.getEmailCode(UserConstants.TYPE_REGISTER, loginUser.getEmail());
        if (StringUtils.isEmpty(code) || !code.equals(loginUser.getCode())) {
            return Result.error(StatusCodeEnum.VERIFY_CODE_ERROR);
        }
        RLock lock = redissonClient.getLock("registerLock:" + loginUser.getEmail());
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                User dbUser = userMapper.getUserByEmail(loginUser.getEmail());
                if (dbUser != null) {
                    return Result.error(StatusCodeEnum.EMAIL_ALREADY_EXIST);
                }
                dbUser = userMapper.getUserByUserName(loginUser.getUserName());
                if (dbUser != null) {
                    return Result.error(StatusCodeEnum.USER_ALREADY_EXIST);
                }
                User user = new User();
                user.setId(IdUtil.getSnowflake(1, 1).nextId());
                user.setUserName(loginUser.getUserName());
                user.setEmail(loginUser.getEmail());
                user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
                user.setAvatar(UserConstants.DEFAULT_AVATAR);
                user.setRole(UserConstants.DEFAULT_ROLE);
                user.setNickName(UserConstants.DEFAULT_NICKNAME_PREFIX + RandomStringUtils.random(5, true, true));
                int flag = userMapper.insert(user);
                if (flag > 0) {
                    return Result.success(new LoginUserVO(user.getId(), userToJWT(user)));
                }
            }
            return Result.error(StatusCodeEnum.ERROR);
        } catch (Exception e) {
            log.error("register error: {}", e);
            return Result.error(StatusCodeEnum.ERROR);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Result<LoginUserVO> login(LoginUser loginUser) {
        User user = BeanUtil.copyProperties(loginUser, User.class);
        if (StringUtils.isAllBlank(user.getUserName(), user.getEmail())){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        if (StringUtils.isNotEmpty(user.getUserName())){
            String md5pwd = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
            user.setPassword(md5pwd);
            User dbUser = userMapper.select(user);
            if (dbUser != null){
                LoginUserVO userVO = new LoginUserVO(dbUser.getId(), userToJWT(dbUser));
                return Result.success(userVO);
            }
            return Result.error(StatusCodeEnum.USER_OR_PASSWORD_ERROR);
        }else {
            String code = redisService.getEmailCode(UserConstants.TYPE_LOGIN, user.getEmail());
            if (StringUtils.isEmpty(code) || !code.equals(loginUser.getCode())) {
                return Result.error(StatusCodeEnum.VERIFY_CODE_ERROR);
            }
            User dbUser = userMapper.select(user);
            if (dbUser == null){
                return Result.error(StatusCodeEnum.EMAIL_NOT_REGISTERED);
            }
            LoginUserVO userVO = new LoginUserVO(dbUser.getId(), userToJWT(dbUser));
            return Result.success(userVO);
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
            log.error("send email error: {}", e);
        }
    }
}
