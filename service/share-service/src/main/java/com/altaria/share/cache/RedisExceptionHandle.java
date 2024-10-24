package com.altaria.share.cache;

import com.altaria.redis.CheckConnectTask;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@Aspect
public class RedisExceptionHandle {

    @Autowired
    private CheckConnectTask checkConnectTask;


    @Around("execution(* com.altaria.share.cache.ShareCacheService.*(..))")
    public Object handleRedisException(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!checkConnectTask.isRedisConnected()) {
            log.warn("Redis not connected, using default values");
            return getDefaultReturnValue(joinPoint.getSignature());
        }

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            log.warn("Exception in method {}: {}", joinPoint.getSignature(), e.getMessage());
            return getDefaultReturnValue(joinPoint.getSignature());
        }
    }

    private Object getDefaultReturnValue(Object signature) {
        Class<?> returnType = ((MethodSignature) signature).getReturnType();
        if (returnType == Boolean.class || returnType == boolean.class) {
            return false;
        } else if (returnType == Long.class || returnType == long.class) {
            return 0L;
        } else if (returnType == Integer.class || returnType == int.class) {
            return 0;
        } else if (returnType == List.class) {
            return Collections.emptyList();
        } else {
            return null;
        }
    }
}
