package com.altaria.share.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
public class CheckAspect {

    @Pointcut("@annotation(com.altaria.common.annotation.CheckCookie)")
    public void cut() {}

    @Before("cut()")
    public void checkCookie(JoinPoint joinPoint) {
        Arrays.stream(joinPoint.getArgs()).forEach(System.out::println);
    }
}
