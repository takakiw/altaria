package com.altaria.user.cache;

import com.altaria.common.pojos.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserCacheService {

    private static final Long USER_EXPIRATION_TIME = 60 * 60 * 24 * 7L; // 7 days

    private static final Long CODE_EXPIRATION_TIME = 60 * 2L; // 2 minutes
    private static final String EMAIL_CODE_PREFIX = "code:";
    private static final String USER_PREFIX = "user:";

    private static final long USER_NOT_EXIST_EXPIRATION_TIME = 60 * 2L;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveEmailCode(String type, String code, String email) {
        redisTemplate.opsForValue().set(EMAIL_CODE_PREFIX + type + ":" + email, code, CODE_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    /**
     * 获取用户验证码
     * @param type 验证码类型
     * @param email 用户邮箱
     * @return 验证码
     */
    public String getEmailCode(String type, String email) {
        return (String) redisTemplate.opsForValue().get(EMAIL_CODE_PREFIX + type + ":" + email);
    }



    /**
     * 获取验证码的剩余有效时间
     * @param type 验证码类型
     * @param email 用户邮箱
     * @return 剩余时间（秒）
     */
    public int getEmailCodeTTL(String type, String email) {
        Long expire = redisTemplate.getExpire(EMAIL_CODE_PREFIX + type + ":" + email, TimeUnit.SECONDS);
        return expire != null ? expire.intValue() : 0;
    }

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户对象
     */
    public User getUserById(Long userId) {
        return (User) redisTemplate.opsForValue().get(USER_PREFIX + userId);
    }

    /**
     * 保存用户信息到redis
     * @param user 用户对象
     */
    public void saveUser(User user) {
        redisTemplate.opsForValue().set(USER_PREFIX + user.getId(), user, USER_EXPIRATION_TIME, TimeUnit.SECONDS);
    }

    /**
     * 删除用户信息
     * @param userId 用户ID
     */
    public void deleteUser(Long userId) {
        redisTemplate.delete(USER_PREFIX + userId);
    }

    public void setUserNotExist(Long queryId) {
        redisTemplate.opsForValue().set(USER_PREFIX + queryId, new User(), USER_NOT_EXIST_EXPIRATION_TIME, TimeUnit.SECONDS);
    }
}
