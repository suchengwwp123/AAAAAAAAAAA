package com.example.authority.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.example.authority.common.Constant;
import com.example.authority.common.Result;
import com.example.authority.entity.User;
import com.example.authority.enums.EnvEnum;
import com.example.authority.service.UserService;
import com.example.authority.utils.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.example.authority.common.Constant.ONLINE_PREFIX;

@Component
@Slf4j
public class DebounceInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    @Value("${spring.profiles.active}")
    private String env;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // 白名单接口（不做拦截）
    private static final Set<String> WHITELIST = Set.of(
            "/api/user/onlinepage/**",
            "/api/ai/stream/**"
    );

    // 防抖时间（毫秒）
    private static final long DEBOUNCE_TIME_MS = 200;

    // 访问频率控制
    private static final long LIMIT_THRESHOLD = 60;        // 60 次
    private static final long LIMIT_WINDOW_SEC = 60;       // 60 秒窗口
    private static final long BAN_DURATION_SEC = 5 * 60;   // 封禁 5 分钟

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        // ========== 1. 黑名单拦截 ==========
        String blacklistKey = "ip:blacklist:" + ip;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            returnJson(response, 403, Result.error("Ip封禁"));
            return false;
        }

        // ========== 2. 白名单放行 ==========
        for (String pattern : WHITELIST) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }

        // ========== 3. 访问频率统计 ==========
        String ipLimitKey = "ip:limit:" + ip;
        Long count = redisTemplate.opsForValue().increment(ipLimitKey);
        if (count != null && count == 1) {
            redisTemplate.expire(ipLimitKey, LIMIT_WINDOW_SEC, TimeUnit.SECONDS);
        }
        //========== 4.判断是否为开发环境，如果是开发环境不封禁ip和账号==========
        if (!EnvEnum.isDev(env)) {
            System.out.println(ipLimitKey);
            if (count != null && count > LIMIT_THRESHOLD) {
                // 设置 IP 黑名单
                redisTemplate.opsForValue().set(blacklistKey, "1", BAN_DURATION_SEC, TimeUnit.SECONDS);

                // 如果用户已登录，则封禁账号并登出
                if (StpUtil.isLogin()) {
                    Long loginId = StpUtil.getLoginIdAsLong();
                    User user = userService.getById(loginId);
                    user.setStatu(0L); // 封禁状态
                    userService.saveOrUpdate(user);
                    StpUtil.logout();
                    redisUtils.hdel(ONLINE_PREFIX, String.valueOf(loginId));
                    redisUtils.hdel(Constant.PERMISSION_PREFIX, String.valueOf(loginId));
                    redisUtils.hdel(Constant.ROLE_PREFIX, String.valueOf(loginId));
                    redisUtils.hdel(Constant.ROUTERS_PREFIX, String.valueOf(loginId));
                }

                returnJson(response, 401, Result.error("账号封禁!"));
                return false;
            }
        }

        // ========== 4. 防抖（200ms） ==========
        String debounceKey = "debounce:" + path + ":" + ip;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(debounceKey))) {
            returnJson(response, 429, Result.error("请求过于频繁，请稍后再试"));
            return false;
        }
        // 设置防抖 Key，200ms 后过期
        redisTemplate.opsForValue().set(debounceKey, "1", DEBOUNCE_TIME_MS, TimeUnit.MILLISECONDS);

        // 放行
        return true;
    }

    private void returnJson(HttpServletResponse response, int status, Result result) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String json = JSONUtil.toJsonStr(result);
        response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }
}
