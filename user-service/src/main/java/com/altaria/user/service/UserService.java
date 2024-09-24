package com.altaria.user.service;

import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.user.entity.User;

public interface UserService {


    Result login(User user);

    Result sendCode(String email);
}
