package com.altaria.share.aspect;

import com.altaria.common.constants.ShareConstants;
import com.altaria.common.pojos.share.entity.Share;
import com.altaria.share.cache.ShareCacheService;
import com.altaria.share.mapper.ShareMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

//@Component
@Aspect
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
        Logger logger = Logger.getLogger(getClass().getName());
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
                    throw new IllegalArgumentException("验证失败");
                }
                cacheService.saveShareInfo(shareInfo);
            }
            if (shareInfo.getUid() == null) {
                throw new IllegalArgumentException("验证失败");
            }
            String dbSign = shareInfo.getSign();
            // 从cookie中获取验证码
            Cookie[] cookies = request.getCookies();
            if (cookies == null || cookies.length == 0) {
                throw new IllegalArgumentException("验证失败");
            }
            List<Cookie> cookieList = Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(ShareConstants.COOKIE_NAME + shareId) && cookie.getValue().equals(dbSign)).toList();
            if (cookieList.size() == 0){
                throw new IllegalArgumentException("验证失败");
            }
            // 验证通过，放行
        }catch(Exception e){
            logger.warning("验证失败: "+ request.getRequestURI());
            throw new IllegalArgumentException("验证失败");
        }
    }
}
