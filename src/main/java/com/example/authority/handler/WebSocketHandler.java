package com.example.authority.handler;


import cn.hutool.json.JSONObject;
import com.example.authority.enums.Subscription;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 处理 WebSocket 连接
 *
 * @author click33
 * @since 2022-2-11
 */
public class WebSocketHandler extends TextWebSocketHandler {


    // 频道ID -> WebSocketSession集合（该频道的所有连接）
    private static final ConcurrentHashMap<String, Set<WebSocketSession>> channelSessions = new ConcurrentHashMap<>();

    // sessionId -> 频道ID，方便断开时移除
    private static final ConcurrentHashMap<String, String> sessionChannelMap = new ConcurrentHashMap<>();


    // 监听：连接开启
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        // 解析 URL 参数中的 channel
        URI uri = session.getUri();
//        System.out.println(uri);
        String channelId = null;
        if (uri != null) {
            List<String> channelList = UriComponentsBuilder.fromUri(uri).build().getQueryParams().get("channel");
            if (channelList != null && !channelList.isEmpty()) {
                channelId = channelList.get(0);
            }
        }

        if (channelId == null || channelId.trim().isEmpty()||!isValidChannel(channelId)) {
           session.close();
           return;
        }

        // 绑定 session 到频道集合
        channelSessions.computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionChannelMap.put(session.getId(), channelId);

        // 连接成功提示
        String tips = "WebSocket 连接成功，sessionId=" + session.getId() + "，频道=" + channelId;
//        System.out.println(tips);
        sendMessage(session, tips);
    }

    // 监听：连接关闭
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String channelId = sessionChannelMap.get(session.getId());
        if (channelId != null) {
            Set<WebSocketSession> sessions = channelSessions.get(channelId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    channelSessions.remove(channelId);
                }
            }
            sessionChannelMap.remove(session.getId());
        }

        String tips = "WebSocket 连接关闭，sessionId=" + session.getId() + "，频道=" + channelId;
//        System.out.println(tips);
    }


    //收到消息后的处理逻辑1
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // 假设消息是JSON格式，包含 channel 和 content 字段
        String payload = message.getPayload();
        // 这里简单示例用 org.json 来解析，你项目没这个包，换成 Jackson 或 fastjson 都行
        String channelId = null;
        String content = null;
        try {
            JSONObject json = new JSONObject(payload);
            channelId = json.getStr("channel", null);
            content = json.getStr("content", null);
        } catch (Exception e) {
            // 非json消息，广播给该连接所在频道
            channelId = sessionChannelMap.get(session.getId());
            content = payload;
        }

        if (channelId == null) {
            channelId = "default";
        }

        String sendMsg = "频道[" + channelId + "] 用户[" + session.getId() + "] 说: " + content;
//        System.out.println(sendMsg);

        // 广播给频道内所有连接
        broadcastToChannel(channelId, sendMsg);
    }

    // 给指定频道的连接发送消息2
    public static void broadcastToChannel(String channelId, String message) {
//        System.out.println(channelId);
        Set<WebSocketSession> sessions = channelSessions.get(channelId);
        if (sessions == null) return;
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        }
    }

    // 向单个连接发送消息3
    public static void sendMessage(WebSocketSession session, String message) {
        try {
//            System.out.println("向sid为：" + session.getId() + "，发送：" + message);
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 向频道里某个用户发送消息
     * @param channelId
     * @param userId
     * @param message
     */
    public static void sendMessageToUserInChannel(String channelId, long userId, String message) {
        Set<WebSocketSession> sessions = channelSessions.get(channelId);
        if (sessions == null || sessions.isEmpty()) {
//            System.out.println("频道[" + channelId + "] 没有任何连接");
            return;
        }

        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) continue;

            Object uidObj = session.getAttributes().get("userId");
            if (uidObj != null && Long.parseLong(uidObj.toString()) == userId) {
                sendMessage(session, message);
//                System.out.println("✅ 向频道[" + channelId + "] 中的用户[" + userId + "] 发送消息成功！");
            }
        }
    }
    /**
     * 检查频道名是否在订阅枚举中
     */
    private boolean isValidChannel(String channelId) {
        try {
            Subscription.valueOf(channelId); // 枚举中找不到会抛异常
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }



}

