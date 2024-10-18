package com.altaria.user.service;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.pojos.user.vo.UserVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {
    Result<UserVO> getUserById(Long userId, Long uId);

    Result uploadAvatar(MultipartFile file, Long uId);

    /*void downloadAvatar(String avatar, HttpServletResponse response);*/

    Result updateUser(User user, Long uId);

    Result sendEmailCode(String email);

}
