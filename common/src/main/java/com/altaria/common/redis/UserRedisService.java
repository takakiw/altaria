package com.altaria.common.redis;

import com.altaria.common.constants.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserRedisService {

    private static final String REDIS_KEY_PREFIX = "user:";
    private static final Long USER_EXPIRATION_TIME = 60 * 60 * 24 * 7L; // 7 days

    private static final String EMAIL_LOGIN_CODE = "email_login_code:";
    private static final String EMAIL_REGISTER_CODE = "email_register_code:";
    private static final Long CODE_EXPIRATION_TIME = 60 * 2L; // 2 minutes

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 保存用户验证码到redis
     * @param code
     * @param email
     */
    public void saveEmailCode(String type, String code, String email) {
        if (type.equals(UserConstants.TYPE_REGISTER)){
            redisTemplate.opsForValue().set(EMAIL_REGISTER_CODE + email, code, CODE_EXPIRATION_TIME, TimeUnit.SECONDS);
        }else if (type.equals(UserConstants.TYPE_LOGIN)){
            redisTemplate.opsForValue().set(EMAIL_LOGIN_CODE + email, code, CODE_EXPIRATION_TIME, TimeUnit.SECONDS);
        }
    }

    /**
     * 获取用户验证码
     * @param email
     * @return
     */
    public String getEmailCode(String type, String email) {
        if (type.equals(UserConstants.TYPE_REGISTER)){
            Object o = redisTemplate.opsForValue().get(EMAIL_REGISTER_CODE + email);
            if (o != null){
                return (String) o;
            }
        }else if (type.equals(UserConstants.TYPE_LOGIN)){
            Object o = redisTemplate.opsForValue().get(EMAIL_LOGIN_CODE + email);
            if (o != null){
                return (String) o;
            }
        }
        return null;
    }
}
