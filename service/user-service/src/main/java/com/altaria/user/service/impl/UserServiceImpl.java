package com.altaria.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.pojos.user.vo.UserVO;
import com.altaria.feign.client.FileServiceClient;
import com.altaria.user.cache.UserCacheService;
import com.altaria.user.mapper.UserMapper;
import com.altaria.user.service.UserService;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;


    @Autowired
    private UserCacheService cacheService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private FileServiceClient fileServiceClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Override
    public Result<UserVO> getUserById(Long userId, Long uId) {
        // 判断是否登录，和当前用户是否一致
        if (userId == null || (uId == null && userId.compareTo(-1L) == 0)){
            return Result.error(StatusCodeEnum.UNAUTHORIZED);
        }
        Long queryId = userId.compareTo(-1L) == 0? uId : userId;
        User user = cacheService.getUserById(queryId);
        if (user == null) {
            user = userMapper.getUserById(queryId);
            if (user == null) {
                cacheService.setUserNotExist(queryId);
                return Result.error(StatusCodeEnum.USER_NOT_EXIST);
            }
            cacheService.saveUser(user);
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        if (uId != null && uId.compareTo(queryId) == 0){
            return Result.success(userVO);
        }else {
            // 加密username 例如abcde => a****
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
        if (file.isEmpty() || !file.getContentType().contains("image") || file.getSize() > UserConstants.MAX_AVATAR_SIZE) {
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }

        try {
            User user = userMapper.getUserById(uId);
            if (user == null) {
                return Result.error(StatusCodeEnum.USER_NOT_EXIST);
            }
            String newAvatarName = fileServiceClient.uploadImage(file);
            if (StringUtils.isBlank(newAvatarName)){
                return Result.error(StatusCodeEnum.ERROR);
            }
            if (user.getAvatar() != null && !user.getAvatar().equals(UserConstants.DEFAULT_AVATAR)){
                rabbitTemplate.convertAndSend("delete-avatar-queue", user.getAvatar());
            }
            User dbUser = new User();
            dbUser.setId(uId);
            dbUser.setAvatar(newAvatarName);
            userMapper.updateUser(dbUser);
            cacheService.deleteUser(uId);
            return Result.success(newAvatarName);
        } catch (Exception e) {
            return  Result.error();
        }
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
            String emailCode = cacheService.getEmailCode(UserConstants.TYPE_UPDATE_PWD, user.getEmail());
            if (StringUtils.isEmpty(emailCode) || !emailCode.equals(user.getCode())) {
                return Result.error(StatusCodeEnum.VERIFY_CODE_ERROR);
            }
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
        userMapper.updateUser(user);
        cacheService.deleteUser(uId);
        return Result.success();
    }

    @Override
    public Result sendEmailCode(String email) {
        if(!email.matches(UserConstants.EMAIL_REGEX)){
            return Result.error(StatusCodeEnum.PARAM_ERROR);
        }
        Long emailCodeTTL = cacheService.getEmailCodeTTL(UserConstants.TYPE_UPDATE_PWD, email);
        if (emailCodeTTL != null && emailCodeTTL.intValue() - 60 > 0){
            return Result.error(StatusCodeEnum.SEND_FREQUENTLY);
        }
        threadPoolTaskExecutor.execute(() -> {
            String code = RandomStringUtils.random(6, true, true);
            cacheService.saveEmailCode(UserConstants.TYPE_UPDATE_PWD,code, email);
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
