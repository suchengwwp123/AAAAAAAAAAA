package com.example.authority.aspect;



import com.example.authority.annotations.EnvLimit;
import com.example.authority.enums.EnvEnum;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Slf4j
public class EnvLimitAspect {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Around("@annotation(com.example.authority.annotations.EnvLimit)")
    public Object checkEnv(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        EnvLimit envLimit = method.getAnnotation(EnvLimit.class);
        List<String> allowedEnvs = Arrays.asList(envLimit.allow());
        if (!allowedEnvs.contains(activeProfile)) {
            log.warn("接口 [{}] 当前环境 [{}] 不允许访问！", method.getName(), activeProfile);
            throw new RuntimeException("当前环境禁止调用该接口！");
        }

        return joinPoint.proceed();
    }
}
