package com.altaria.user.service;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.LoginUser;
import com.altaria.common.pojos.user.entity.User;
import com.altaria.common.pojos.user.vo.LoginUserVO;

public interface LoginService {


    User login(LoginUser user);
    void sendCode(String email, String type, String code);

    User register(LoginUser user);

    User getUserByEmail(String email);

    User getUserByUserName(String userName);
}
