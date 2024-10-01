package com.altaria.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.pojos.user.vo.UserVO;
import com.altaria.minio.service.MinioService;
import com.altaria.user.mapper.UserMapper;
import com.altaria.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
            return Result.success();
        } catch (IOException e) {
            return  Result.error();
        }
    }

    @Override
    public void downloadAvatar(String avatar, HttpServletResponse response) {
        minioService.downloadFile(avatar, response);
    }
}
