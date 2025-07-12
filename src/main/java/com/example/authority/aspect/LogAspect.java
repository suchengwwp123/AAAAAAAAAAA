package com.example.authority.aspect;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONUtil;
import com.example.authority.common.Result;
import com.example.authority.entity.Log;
import com.example.authority.annotations.IgnoreParam;
import com.example.authority.annotations.IgoreResult;

import com.example.authority.service.LogService;
import com.example.authority.service.UserService;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;


/**
 * @program: authority-2026.0.3
 * @ClassName: LogAspect
 * @description: 日志注解拦截器
 * @author:dyy
 * @Version 3.0
 **/
@Slf4j
@Component
@Aspect
public class LogAspect {
    @Resource
    private LogService logService;
    @Resource
    private UserService userService;

    @Around("execution(* com.example.authority.controller.*.*(..)) ")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {


         // 设置username用户名
         String username = null;

         // 获取方法名和参数
         String method = joinPoint.getSignature().getName();
         String params = null;
         // 检查是否有IgnoreParm忽略参数注解,如果没有将请求的参数转成数组
         if (!joinPoint.getTarget().getClass().getMethod(method, getParameterTypes(joinPoint)).isAnnotationPresent(IgnoreParam.class)){
             params= Arrays.toString(joinPoint.getArgs());
         }


         // 记录开始时间
         long startTime = System.currentTimeMillis();

         // 获取返回结果
         Object result =  joinPoint.proceed();;
         // 判断是否登录,如果登录设置用户名
         if (StpUtil.isLogin()) {
             try {
                 username=userService.getUserInfo().getUsername();
             }catch (Exception e){
                 SaHolder.getResponse().setStatus(HttpStatus.UNAUTHORIZED.value());
                 return SaResult.error();
             }
         }
         // 记录结束时间
         long endTime = System.currentTimeMillis();

         // 计算执行时间
         long time = endTime - startTime;

         // 获取客户端IP地址
         String ip = this.getRequestIpAddress();
         // 获取请求方法类型
         HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
         String httpMethod = request.getMethod();

        String userAgent = request.getHeader("User-Agent");
        String browser = getBrowserType(userAgent);
        String os = getOperatingSystem(userAgent);

         // 创建日志记录对象
         Log logRecord = new Log();
         logRecord.setUsername(username);
         logRecord.setOperation(httpMethod);
         logRecord.setMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + method + "()");
         logRecord.setParams(params);
         logRecord.setTaketime(String.valueOf(time));
         logRecord.setIp(ip);
        logRecord.setBrowser(browser); // 假设你有 browser 字段
        logRecord.setOs(os);           // 假设你添加了 os 字段
//        判断该请求方法是否有包含IgoreResult注解，如果没有给logRecord对象设置result属性
         if (!joinPoint.getTarget().getClass().getMethod(method, getParameterTypes(joinPoint)).isAnnotationPresent(IgoreResult.class)){

             logRecord.setResult(String.valueOf(result));
         }


         logService.save(logRecord);
         // 将日志记录保存到数据库中

//         System.out.printf(String.valueOf(1/0));

         return result;

    }

    /**
     * 获取客;户端Ip地址
     * @return
     */
    private String getRequestIpAddress() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // 从X-Forwarded-For中获取真实IP地址
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            // 如果X-Forwarded-For未设置，从其他头中获取
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            // 如果以上头信息都未设置，直接从请求中获取
            ip = request.getRemoteAddr();
        }

        // 多个代理时，取第一个IP地址
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)){
            ip="127.0.0.1";
        }

        return ip;
    }

    /**
     * 获取方法参数类型
     * @param joinPoint
     * @return
     */
    private Class<?>[] getParameterTypes(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getParameterTypes();
    }
    /**
     * 简单解析浏览器类型
     */
    private String getBrowserType(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        UserAgent userAgents = UserAgent.parseUserAgentString(userAgent);


        // 获取客户端浏览器
        String browser = userAgents.getBrowser().getName();
        return browser;
    }
    /**
     * 解析操作系统
     */
    private String getOperatingSystem(String userAgent) {
        UserAgent userAgents = UserAgent.parseUserAgentString(userAgent);
        // 获取客户端操作系统
        String os = userAgents.getOperatingSystem().getName();
        return os;
    }


}
