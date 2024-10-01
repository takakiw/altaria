package com.altaria.common.pojos.user.vo;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class UserVO implements Serializable {
    private Long id;
    private String userName;
    private String email;
    private String nickName;
    private String avatar;
    private Integer role;
}
