package com.altaria.common.pojos.user.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;


import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
public class User implements Serializable {
    @NotNull(message = "用户ID不能为空")
    private Long id;
    private String userName;
    @Length(min = 6, max = 20, message = "密码长度必须在6到20之间")
    private String password;
    private String email;
    @Length(min = 2, max = 20, message = "昵称长度必须在2到20之间")
    private String nickName;
    private String avatar;
    private Integer role;
    @Length(min = 6, max = 6, message = "验证码长度必须为6位")
    private String code;
    private Long useSpace;
    private Long totalSpace;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
