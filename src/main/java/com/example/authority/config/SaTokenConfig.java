package com.example.authority.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONUtil;
//import com.example.authority.config.properties.XssProperties;
//import com.example.authority.filter.XssFilter;
import com.example.authority.config.properties.XssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @program: authority-2026.0.3
 * @ClassName:SaTokenConfig
 * @description: [Sa-Token 权限认证] 配置类
 * @author:dyy
 * @Version 3.0
 **/
@Configuration
@Slf4j
public class SaTokenConfig implements WebMvcConfigurer {

    @Autowired
    private XssProperties xssProperties;
//    @Autowired
//    private XssFilter xssFilter;
    /**
     * 注册 [Sa-Token全局过滤器]
     */
    @Bean
    public SaServletFilter getSaServletFilter() {
        return new SaServletFilter()
                // 指定 拦截路由 与 放行路由
                .addInclude("/**")
                .addExclude("/favicon.ico")
                .addExclude("/swagger-ui/**")
                .addExclude("/v3/api-docs/**")
                .addExclude("/auth/captcha")
                .addExclude("/druid/**")
                .addExclude("/ws-connect/**")
                .addExclude("/cache/**")
                .addExclude("/file/**")
                .addExclude("/auth/register")
                .addExclude("/alipay/**")
                .addExclude("/auth/public-key")
                .addExclude("/auth/email/**")
                .addExclude("/auth/islogin")
                .addExclude("/auth/findpassword/**")

                /* 排除掉 /favicon.ico /swagger-ui/***/
                // 认证函数: 每次请求执行都会进行拦截
                .setAuth(obj -> {

                    // 输出 API 请求日志，方便调试代码
//                    SaManager.getLog().debug("----- 请求path={}  提交token={}", SaHolder.getRequest().getRequestPath(), StpUtil.getTokenValue());
                    // 登录认证 -- 拦截所有路由，并排除/user/doLogin 用于开放登录

                    SaRouter.match("/**", "/auth/login", () -> StpUtil.checkLogin());

                    // 更多拦截处理方式，请参考“路由拦截式鉴权”章节 */
                })

                // 异常处理函数：每次认证函数发生异常时执行此函数
                .setError(e -> {

                    // 设置响应头
                    SaHolder.getResponse().setHeader("Content-Type", "application/json;charset=UTF-8");
                    SaHolder.getResponse().setStatus(HttpStatus.UNAUTHORIZED.value());
                    // 使用封装的 JSON 工具类转换数据格式
                    return JSONUtil.toJsonStr(SaResult.error(e.getMessage()));
                })

                // 前置函数：在每次认证函数之前执行（BeforeAuth 不受 includeList 与 excludeList 的限制，所有请求都会进入），还有处理跨域请求的
                .setBeforeAuth(r -> {
                    // ---------- 设置一些安全响应头 ----------
                    SaHolder.getResponse().setHeader("X-Frame-Options", "ALLOWALL") // Sa-Token 虽然支持，但部分浏览器不认
                            .setHeader("Content-Security-Policy", "frame-ancestors *").
                            setHeader("Access-Control-Allow-Origin", "*").
                            setHeader("Access-Control-Allow-Methods", "*").
                            setHeader("Access-Control-Allow-Headers", "*").
                            setHeader("Access-Control-Max-Age", "3600");
                    // 如果是预检请求，则立即返回到前端
                    SaRouter.match(SaHttpMethod.OPTIONS)

                            .back();

                });
    }



    // 注册 Sa-Token 拦截器，打开注解式鉴权功能
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }




    /**
     * 引入密码加密类
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}