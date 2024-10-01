package com.altaria.common.pojos.user.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class LoginUser {
    private String userName;
    private String password;
    private String email;
    private String nickName;
    private String code;
}
