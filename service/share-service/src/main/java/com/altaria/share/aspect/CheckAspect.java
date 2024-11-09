package com.altaria.share.aspect;

import com.altaria.common.constants.ShareConstants;
import com.altaria.common.constants.UserConstants;
import com.altaria.common.pojos.share.entity.Share;
import com.altaria.config.exception.BaseException;
import com.altaria.share.cache.ShareCacheService;
import com.altaria.share.mapper.ShareMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Component
@Aspect
@Slf4j
public class CheckAspect {

    @Pointcut("@annotation(com.altaria.common.annotation.CheckCookie)")
    public void cut() {}

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ShareCacheService cacheService;

    @Autowired
    private ShareMapper shareMapper;

    @Before("cut()")
    public void checkCookie(JoinPoint joinPoint) {
        String uid = request.getHeader(UserConstants.USER_ID);
        try{
            // params中的shareId参数(第一个参数)
            Object arg = joinPoint.getArgs()[0];
            Long shareId = Long.parseLong(arg.toString());
            // 从缓存中获取验证码
            Share shareInfo = cacheService.getShareInfo(shareId);
            if (shareInfo == null){
                shareInfo = shareMapper.getShareById(shareId);
                if (shareInfo == null){
                    cacheService.saveNullShareInfo(shareId);
                    throw new BaseException("验证失败");
                }
                cacheService.saveShareInfo(shareInfo);
            }
            if (shareInfo.getUid() == null) {
                throw new BaseException("验证失败");
            }
            if (uid != null && shareInfo.getUid().compareTo(Long.parseLong(uid)) == 0){
                // 验证通过，放行
                return;
            }
            String dbSign = shareInfo.getSign();
            if(dbSign != null && !StringUtils.isBlank(dbSign)){
                // 从cookie中获取验证码
                Cookie[] cookies = request.getCookies();
                if (cookies == null || cookies.length == 0) {
                    throw new BaseException("验证失败");
                }
                List<Cookie> cookieList = Arrays.stream(cookies)
                        .filter(cookie -> cookie.getName().equals(ShareConstants.COOKIE_NAME + shareId) && cookie.getValue().equals(dbSign)).toList();
                if (cookieList.size() == 0){
                    throw new BaseException("验证失败");
                }
                // 验证通过，放行
                log.info("验证通过: "+ request.getRequestURI());
                return;
            }
        }catch(Exception e){
            log.warn("验证失败: "+ request.getRequestURI());
            throw new BaseException("验证失败");
        }
    }
}
