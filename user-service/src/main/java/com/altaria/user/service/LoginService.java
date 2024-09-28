package com.altaria.user.service;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.LoginUser;

public interface LoginService {


    Result login(LoginUser user);

    Result sendCode(String email, String type);

    Result register(LoginUser user);
}
