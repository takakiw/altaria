package com.altaria.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.pojos.user.vo.UserSpaceVO;
import com.altaria.common.pojos.user.vo.UserVO;
import com.altaria.minio.service.MinioService;
import com.altaria.redis.RedisService;
import com.altaria.user.mapper.UserMapper;
import com.altaria.user.service.UserService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MinioService minioService;

    @Autowired
    private RedisService redisService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private HttpServletRequest request;

    @Override
    public Result<UserVO> getUserById(Long userId, Long uId) {
        // 判断是否登录，和当前用户是否一致
        if (userId == null || (uId == null && userId.compareTo(-1L) == 0)){
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        Long queryId = userId.compareTo(-1L) == 0? uId : userId;
        User user = null;
        user = redisService.getUserById(queryId);
        if (user == null) {
            user = userMapper.getUserById(queryId);
            if (user == null) {
                return Result.error(StatusCodeEnum.USER_NOT_EXIST);
            }
            redisService.saveUser(user);
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        if (uId != null && uId.compareTo(queryId) == 0){
            return Result.success(userVO);
        }else {
            userVO.setEmail(null);
            userVO.setRole(null);
            return Result.success(userVO);
        }
    }

    @Override
    public Result uploadAvatar(MultipartFile file, Long uId) {
        if (uId == null) {
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        if (file.isEmpty() || !file.getContentType().contains("image")) {
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;
        try {
            User user = userMapper.getUserById(uId);
            if (user == null) {
                return Result.error(StatusCodeEnum.USER_NOT_EXIST);
            }
            minioService.upLoadFile(newFileName, file.getInputStream(), file.getContentType());
            if (user.getAvatar() != null && !user.getAvatar().equals(UserConstants.DEFAULT_AVATAR)){
                minioService.deleteFile(user.getAvatar());
                //日志记录
            }
            user.setAvatar(newFileName);
            userMapper.updateUser(user);
            redisService.deleteUser(uId);
            return Result.success(newFileName);
        } catch (Exception e) {
            return  Result.error();
        }
    }

    @Override
    public void downloadAvatar(String avatar, HttpServletResponse response) {
        if (StringUtils.isBlank(avatar)){
            return;
        }
        minioService.downloadFile(avatar, response);
    }

    @Override
    public Result updateUser(User user, Long uId) {
        if (uId == null) {
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        user.setId(uId);
        if (user.getNickName() != null && StringUtils.isBlank(user.getNickName())){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        if (StringUtils.isNotEmpty(user.getPassword())){
            String emailCode = redisService.getEmailCode(UserConstants.TYPE_UPDATE_PWD, user.getEmail());
            if (StringUtils.isEmpty(emailCode) || !emailCode.equals(user.getCode())) {
                return Result.error(StatusCodeEnum.VERIFY_CODE_ERROR);
            }
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
        userMapper.updateUser(user);
        redisService.deleteUser(uId);
        return Result.success();
    }

    @Override
    public Result sendEmailCode(String email) {
        if(!email.matches(UserConstants.EMAIL_REGEX)){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        if (redisService.getEmailCodeTTL(UserConstants.TYPE_UPDATE_PWD, email) - 60 > 0){
            return Result.error(StatusCodeEnum.SEND_FREQUENTLY);
        }
        threadPoolTaskExecutor.execute(() -> {
            String code = RandomStringUtils.random(6, true, true);
            redisService.saveEmailCode(UserConstants.TYPE_UPDATE_PWD,code, email);
            String text = UserConstants.EMAIL_UPDATE_PWD_TEXT;
            String subject = UserConstants.EMAIL_UPDATE_PWD_SUBJECT;
            sendEmail(email,subject, String.format(text, code));
        });
        return Result.success();
    }

    @Override
    public Result getSpaceUserById(Long uId) {
        if (uId == null){
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        User user = redisService.getUserById(uId);
        if (user == null){
            user = userMapper.getUserById(uId);
            if (user == null){
                return Result.error(StatusCodeEnum.USER_NOT_EXIST);
            }
            redisService.saveUser(user);
        }
        UserSpaceVO spaceVO = BeanUtil.copyProperties(user, UserSpaceVO.class);
        return Result.success(spaceVO);
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
