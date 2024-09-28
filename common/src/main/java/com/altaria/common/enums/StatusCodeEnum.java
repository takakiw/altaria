package com.altaria.common.enums;

public enum StatusCodeEnum {
    SUCCESS(200,"操作成功"),
    ERROR(500,"操作失败"),

    // 参数错误
    PARAM_ERROR(1001,"参数错误"),
    PARAM_NOT_NULL(1002,"参数不能为空"),

    // 业务错误
    EMAIL_NOT_EXIST(2001,"邮箱不存在"),
    USER_NOT_EXIST(2002,"用户不存在"),
    USER_ALREADY_EXIST(2003,"用户已存在"),
    PASSWORD_ERROR(2004,"密码错误"),
    EMAIL_ALREADY_EXIST(2005,"邮箱已经被使用"),
    USER_OR_PASSWORD_ERROR(2006,"用户名或密码错误"),
    VERIFY_CODE_ERROR(2007,"验证码错误或已失效"),
    VERIFY_CODE_EXPIRED(2008,"验证码已过期"),
    VERIFY_CODE_SEND_FAILED(2009,"验证码发送失败");


    int code;
    String msg;

    StatusCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return msg;
    }
}
