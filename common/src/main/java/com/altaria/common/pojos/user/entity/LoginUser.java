package com.altaria.common.pojos.user.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@ToString
public class LoginUser {
    @Length(min = 3, max = 20, message = "用户名长度必须在3到20之间")
    private String userName;
    @Length(min = 6, max = 20, message = "密码长度必须在6到20之间")
    private String password;
    private String email;
    @Length(min = 2, max = 20, message = "昵称长度必须在2到20之间")
    private String nickName;
    @Length(min = 6, max = 6, message = "验证码长度必须为6")
    private String code;
}
