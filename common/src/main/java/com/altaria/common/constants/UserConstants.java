package com.altaria.common.constants;

public class UserConstants {
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String USER_NICKNAME = "userNickname";
    public static final String USER_EMAIL = "userEmail";
    public static final String EMAIL_LOGIN_SUBJECT = "登录验证码";
    public static final String EMAIL_LOGIN_TEXT = "您正在登录Altaria云平台，您的验证码是：<h1 style='color:blue;'>%s</h1>，请在2分钟内输入。如非本人操作，请忽略本邮件。";

    public static final String EMAIL_REGISTER_SUBJECT = "注册验证码";
    public static final String EMAIL_REGISTER_TEXT = "您正在注册Altaria云平台，您的验证码是：<h1 style='color:blue;'>%s</h1>，请在2分钟内输入。如非本人操作，请忽略本邮件。";
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_-]{6,12}+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    
    public static final String DEFAULT_AVATAR = "3494146119fHjZSCaucS_.jpeg";

    public static final String TYPE_LOGIN = "login";
    public static final String TYPE_REGISTER = "register";


    public static final int DEFAULT_ROLE = 0;
    public static final String DEFAULT_NICKNAME = "cloud用户_";
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]{4,20}$";
    public static final String TYPE_UPDATE_PWD = "updatePwd";
    public static final String EMAIL_UPDATE_PWD_TEXT = "您正在修改Altaria云平台密码，您的验证码是：<h1 style='color:blue;'>%s</h1>，请在2分钟内输入。如非本人操作，请忽略本邮件。";
    public static final String EMAIL_UPDATE_PWD_SUBJECT = "修改密码验证码";
}
