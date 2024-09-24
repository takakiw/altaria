package com.altaria.common.pojos.user.entity;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
public class User implements Serializable {
    private Integer id;
    private String userName;
    private String password;
    private String email;
    private String nickName;
    private String avatar;
    private String role;
    private LocalDateTime createdTime;
}
