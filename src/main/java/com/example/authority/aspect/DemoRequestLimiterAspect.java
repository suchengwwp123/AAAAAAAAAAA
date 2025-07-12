package com.example.authority.aspect;

import com.example.authority.enums.EnvEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Aspect
@Component
@Slf4j
public class DemoRequestLimiterAspect {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    // 支持通配符路径匹配
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // ✅ 允许在 demo 环境访问的 POST 接口路径（支持通配符）
    private static final List<String> POST_WHITELIST = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/logout"


    );

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object checkDemoRequestLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!EnvEnum.DEMO.getCode().equalsIgnoreCase(activeProfile)) {
            return joinPoint.proceed(); // 非 demo 环境放行
        }

        // 获取当前请求
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return joinPoint.proceed();
        HttpServletRequest request = attrs.getRequest();
        String method = request.getMethod().toUpperCase();
        String uri = request.getRequestURI();

        // ✅ 放行所有 GET 请求
        if ("GET".equals(method)) {
            return joinPoint.proceed();
        }

        // ✅ 放行白名单中的 POST 请求（支持通配符）
        if ("POST".equals(method)) {
            boolean match = POST_WHITELIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
            if (match) {
                return joinPoint.proceed();
            }
        }

        log.warn("【演示环境限制】拦截了 [{}] 请求：{}", method, uri);
        throw new RuntimeException("演示环境禁止该操作！");
    }
}
