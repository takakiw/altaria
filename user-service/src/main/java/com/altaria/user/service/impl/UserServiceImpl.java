package com.altaria.user.service.impl;

import com.altaria.common.exception.BaseException;
import com.altaria.common.pojos.common.Result;
import com.altaria.user.mapper.UserMapper;
import com.altaria.common.redis.UserRedisService;
import com.altaria.user.service.UserService;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.utils.JWTUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRedisService userRedisService;

    @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String from;

    @Override
    public Result sendCode(String email) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            mimeMessage.setFrom(from);
            mimeMessage.setSubject("验证码");
            String code = RandomStringUtils.random(6, true, true);
            userRedisService.saveCode(code, email);
            mimeMessage.setText("您的验证码为：" + code);
            mimeMessage.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(email));
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            return Result.error("验证码发送失败");
        }
        return Result.success();
    }

    @Override
    public Result login(User user) {
        // 校验用户名或邮箱
        if (StringUtils.isAllBlank(user.getUserName(), user.getEmail())){
            return Result.error("用户名或邮箱不能为空");
        }
        // 使用用户名登录
        if (StringUtils.isNotEmpty(user.getUserName())){
            // MD5加密密码
            String md5pwd = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
            user.setPassword(md5pwd);
            User dbUser = userMapper.select(user);
            if (dbUser != null){
                Map<String, Object> mp = new HashMap<>();
                mp.put("id", dbUser.getId());
                mp.put("userName", dbUser.getUserName());
                mp.put("email", dbUser.getEmail());
                mp.put("nickName", dbUser.getNickName());
                String jwt = JWTUtil.generateJwt(mp);
                return Result.success(jwt);
            }
            return Result.error("用户名或密码错误");
        }else { // 使用邮箱登录
            String code = userRedisService.getCode(user.getEmail());
            if (StringUtils.isEmpty(code) || !code.equals(user.getPassword())) {
                return Result.error("验证码过期或错误");
            }
            user.setPassword(null);
            User dbUser = userMapper.select(user);
            if (dbUser == null){
                return Result.error("邮箱不存在");
            }
            Map<String, Object> mp = new HashMap<>();
            mp.put("id", dbUser.getId());
            mp.put("userName", dbUser.getUserName());
            mp.put("email", dbUser.getEmail());
            mp.put("nickName", dbUser.getNickName());
            String jwt = JWTUtil.generateJwt(mp);
            return Result.success(jwt);
        }
    }
}
