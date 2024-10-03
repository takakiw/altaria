package com.altaria.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.pojos.user.vo.UserVO;
import com.altaria.common.utils.BaseContext;
import com.altaria.minio.service.MinioService;
import com.altaria.redis.UserRedisService;
import com.altaria.user.mapper.UserMapper;
import com.altaria.user.service.UserService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MinioService minioService;

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
    public Result<UserVO> getUserById(Long userId) {
        User user = userMapper.getUserById(userId);
        if (user == null){
            return Result.error(StatusCodeEnum.USER_NOT_EXIST);
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        return Result.success(userVO);
    }

    @Override
    public Result uploadAvatar(MultipartFile file, Long uId) {
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
        try {
            minioService.upLoadFile(newFileName, file.getInputStream(), file.getContentType());
            User user = new User();
            user.setId(uId);
            user.setAvatar(newFileName);
            userMapper.updateUser(user);
            return Result.success(newFileName);
        } catch (IOException e) {
            return  Result.error();
        }
    }

    @Override
    public void downloadAvatar(String avatar, HttpServletResponse response) {
        minioService.downloadFile(avatar, response);
    }

    @Override
    public Result updateUser(User user) {
        Long uId = BaseContext.getCurrentId();
        if (uId == null) {
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        if (user == null || user.getId() == null || user.getId().longValue() != uId.longValue()) {
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        if (user.getNickName() != null && user.getNickName().length() == 0){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }

        if (StringUtils.isNotEmpty(user.getPassword())){
            String emailCode = userRedisService.getEmailCode(UserConstants.TYPE_UPDATE_PWD, user.getEmail());
            if (emailCode == null) {
                return Result.error(StatusCodeEnum.EMAIL_CODE_EXPIRED);
            }
            if (!user.getCode().equals(emailCode)){
                return Result.error(StatusCodeEnum.EMAIL_CODE_EXPIRED);
            }
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
        userMapper.updateUser(user);
        return Result.success();
    }

    @Override
    public Result sendEmailCode(String email) {
        if(!email.matches(UserConstants.EMAIL_REGEX)){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        if (userRedisService.getEmailCodeTTL(UserConstants.TYPE_UPDATE_PWD, email) - 60 > 0){
            return Result.error(StatusCodeEnum.SEND_FREQUENTLY);
        }
        threadPoolTaskExecutor.execute(() -> {
            String code = RandomStringUtils.random(6, true, true);
            userRedisService.saveEmailCode(UserConstants.TYPE_UPDATE_PWD,code, email);
            String text = UserConstants.EMAIL_UPDATE_PWD_TEXT;
            String subject = UserConstants.EMAIL_UPDATE_PWD_SUBJECT;
            sendEmail(email,subject, String.format(text, code));
        });
        return Result.success();
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
