package com.altaria.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.LoginUser;
import com.altaria.common.pojos.user.vo.LoginUserVO;
import com.altaria.user.cache.UserCacheService;
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
    private RedissonClient redissonClient;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendCode(String email, String type, String code) {
       threadPoolTaskExecutor.execute(() -> {
           String text = type.equals(UserConstants.TYPE_REGISTER)?UserConstants.EMAIL_REGISTER_TEXT:UserConstants.EMAIL_LOGIN_TEXT;
           String subject = type.equals(UserConstants.TYPE_REGISTER)?UserConstants.EMAIL_REGISTER_SUBJECT:UserConstants.EMAIL_LOGIN_SUBJECT;
           sendEmail(email,subject, String.format(text, code));
       });
    }

    @Override
    public User register(LoginUser loginUser) {
        RLock lock = redissonClient.getLock("registerLock:" + loginUser.getEmail());
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
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
                    return user;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    @Override
    public User getUserByUserName(String userName) {
        return userMapper.getUserByUserName(userName);
    }

    @Override
    public User login(LoginUser loginUser) {
        User queryUser = new User();
        // 用户名和邮箱只能使用一个进行登录
        if (StringUtils.isNotBlank(loginUser.getUserName())) {
            queryUser.setUserName(loginUser.getUserName());
            queryUser.setPassword(DigestUtils.md5DigestAsHex(loginUser.getPassword().getBytes()));
        } else {
            queryUser.setEmail(loginUser.getEmail());
        }
        return userMapper.select(queryUser);
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
