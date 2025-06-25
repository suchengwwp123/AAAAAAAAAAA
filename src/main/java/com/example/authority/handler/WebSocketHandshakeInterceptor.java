package com.example.authority.handler;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手前的拦截器，用于 token 校验等逻辑处理
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * 握手前执行（WebSocket 连接建立前）
     * @param request     原始 HTTP 请求
     * @param response    原始 HTTP 响应
     * @param wsHandler   WebSocket handler
     * @param attributes  可以存储一些信息（例如当前用户）传递到 WebSocket 会话中
     * @return true 表示握手继续，false 拒绝握手
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // 判断是否是 servlet 请求（HTTP请求）
        if (request instanceof ServletServerHttpRequest servletRequest) {
           if (StpUtil.isLogin()){

               attributes.put("token",StpUtil.getTokenValue());
               return true ;
           }
        }

//        System.out.println("❌ WebSocket 鉴权失败，连接已拒绝");
        return false; // 拒绝握手
    }

    /**
     * 握手完成后的回调方法
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
//        System.out.println("🔄 WebSocket 握手已完成");
    }
}
