package com.altaria.common.pojos.user.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
public class User implements Serializable {
    private Long id;
    private String userName;
    private String password;
    private String email;
    private String nickName;
    private String avatar;
    private Integer role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
