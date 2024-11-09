package com.altaria.user.aspect;

import com.altaria.common.annotation.AutoFill;
import com.altaria.common.enums.OperationType;
import com.altaria.config.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j
public class AutoFillAspect {

    @Pointcut("@annotation(com.altaria.common.annotation.AutoFill)) && execution(* com.altaria.user.mappper.*.*(..))")
    public void cut(){}

    @Before("cut()")
    public void before(JoinPoint joinPoint){
        log.info("开始自动填充...");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType value = annotation.value();

        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            return;
        }

        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();
        if (value == OperationType.INSERT){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                setUpdateTime.invoke(entity, now);
                setCreateTime.invoke(entity, now);
            } catch (Exception  e) {
                throw new RuntimeException(e);
            }
        }else if (value == OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                setUpdateTime.invoke(entity, now);
            } catch (Exception  e) {
                throw new BaseException(e.getMessage());
            }
        }

    }

}
