package com.altaria.common.enums;

public enum StatusCodeEnum {

    FILE_UPLOAD_FAILED(101, "文件上传失败"),
    SPACE_NOT_ENOUGH(102,"空间不足"),

    SUCCESS(200,"操作成功"),

    ILLEGAL_REQUEST(400,"非法请求"),
    UNAUTHORIZED(401,"未授权, 请登录"),
    ERROR(500,"服务器内部错误"),

    // 参数错误
    PARAM_ERROR(1001,"参数错误"),
    PARAM_NOT_NULL(1002,"参数不能为空"),

    // 业务错误
    EMAIL_NOT_REGISTERED(2001,"邮箱未注册"),
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
    SEND_FREQUENTLY(2013, "请勿频繁发送"),
    ILLEGAL_FILE_NAME(2014, "文件名非法"),
    FILE_ALREADY_EXISTS(2015,"文件名已存在" ),
    FILE_NOT_EXISTS(2016,"文件不存在"),
    VIDEO_NOT_EXISTS(2017,"视频不存在"),
    FILE_CANNOT_PREVIEW(2018, "文件无法预览"),
    FILE_TRANSCODING(2019, "文件正在转码中, 请稍后再试"),
    GATEWAY_ERROR(2020, "网关错误"),
    DIRECTORY_NOT_EXISTS(2021,"目录不存在"),
    CREATE_SHARE_LINK_ERROR(2022, "创建分享链接失败, 请稍后再试"),
    SHARE_NOT_EXISTS(2023, "分享过期或已经被删除"),
    SHARE_TYPE_ERROR(2024, "分享类型错误"),
    CATEGORY_NOT_FOUND(2025, "分类不存在"),
    CREATE_NOTE_FAILED(2026, "创建笔记失败"),
    NOTE_NOT_FOUND(2027, "笔记不存在"),
    UPDATE_NOTE_FAILED(2028, "更新笔记失败"),
    DELETE_NOTE_FAILED(2029, "删除笔记失败"),
    CATEGORY_NAME_INVALID(2030, "分类名称非法"),
    CATEGORY_ALREADY_EXISTS(2031, "分类已存在"),
    CATEGORY_ADD_FAILED(2032, "添加分类失败"),
    CATEGORY_UPDATE_FAILED(2033, "更新分类失败"),
    CATEGORY_DELETE_FAILED(2034, "删除分类失败"),
    FILE_TRANSCODING_FAILED(2035, "文件转码失败"),
    SHARE_SIGN_ERROR(2036, "分享签名错误"),
    ONLY_FILE_SAVED(2037, "只能保存文件");



    private final int code;
    private final String msg;

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
