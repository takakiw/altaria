package com.altaria.common.pojos.user.vo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
public class UserVO implements Serializable {
    private Integer id;
    private String userName;
    private String email;
    private String nickName;
    private String avatar;
    private int role;
}
