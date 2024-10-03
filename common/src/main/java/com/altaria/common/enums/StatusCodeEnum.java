package com.altaria.common.enums;

public enum StatusCodeEnum {
    SUCCESS(200,"操作成功"),

    ILLEGAL_REQUEST(400,"非法请求"),
    UNAUTHORIZED(401,"未授权, 请登录"),
    ERROR(500,"服务器内部错误"),

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
    VERIFY_CODE_SEND_FAILED(2009,"验证码发送失败"),
    TOKEN_INVALID(2010, "token无效, 请重新登录"),
    USER_NAME_FORMAT_ERROR(2011, "用户名格式错误"),
    EMAIL_CODE_EXPIRED(2012, "邮箱验证码已过期"),
    SEND_FREQUENTLY(2013, "请勿频繁发送");


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
