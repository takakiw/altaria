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
    public static final String EMAIL_REGEX = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    
    public static final String DEFAULT_AVATAR = "http://192.168.96.132:9000/mini/test.jpg";

    public static final String TYPE_LOGIN = "login";
    public static final String TYPE_REGISTER = "register";


    public static final int DEFAULT_ROLE = 0;
    public static final String DEFAULT_NICKNAME = "cloud用户_";
}
