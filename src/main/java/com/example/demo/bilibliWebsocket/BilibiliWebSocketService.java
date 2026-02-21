package com.example.demo.bilibliWebsocket;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;

import jakarta.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class BilibiliWebSocketService implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(BilibiliWebSocketService.class);
    // 存储房间ID到B站客户端的映射
    private final ConcurrentHashMap<Integer, BilibiliClient> roomClients = new ConcurrentHashMap<>();

    // 存储房间ID到用户WebSocket会话的映射
    private final ConcurrentHashMap<Integer, CopyOnWriteArraySet<WebSocketSession>> roomUserSessions =
            new ConcurrentHashMap<>();

    // 存储用户WebSocket会话到其所属房间的反向映射
    private final ConcurrentHashMap<WebSocketSession, Integer> sessionToRoomMap = new ConcurrentHashMap<>();

    private final java.util.Timer timer = new java.util.Timer();
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public BilibiliWebSocketService() {
        // 初始化定时心跳任务
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                sendHeartbeats();
            }
        }, 30000, 30000);
    }

    @PreDestroy
    public void destroy() {
        log.info("销毁BilibiliWebSocketService");
        timer.cancel();
        // 关闭所有B站连接
        for (BilibiliClient client : roomClients.values()) {
            client.disconnect();
        }
        roomClients.clear();
        roomUserSessions.clear();
        sessionToRoomMap.clear();
    }

    public void connectToRoom(int roomId, String cookie, WebSocketSession userSession) throws Exception {
        log.info("用户连接已建立，开始连接房间 {}", roomId);

        // 获取或创建该房间的客户端
        BilibiliClient client = roomClients.computeIfAbsent(roomId, k -> {
            try {
                log.info("创建BilibiliClient，房间ID: {}", roomId);

                CopyOnWriteArraySet<WebSocketSession> userSessions = roomUserSessions.computeIfAbsent(roomId,
                        r -> new CopyOnWriteArraySet<>());
                return new BilibiliClient(roomId, cookie, this, userSessions);
            } catch (Exception e) {
                log.error("创建BilibiliClient失败", e);
                throw new RuntimeException(e);
            }
        });

        // 将用户会话添加到该房间的监听者列表
        CopyOnWriteArraySet<WebSocketSession> sessions = roomUserSessions.computeIfAbsent(roomId, k -> {
            log.info("创建用户会话列表，房间ID: {}", roomId);
            return new CopyOnWriteArraySet<>();
        });

        boolean added = sessions.add(userSession);
        if (added) {
            log.info("已添加用户会话 {} 到房间 {} 的监听者列表", userSession.getId(), roomId);
        } else {
            log.warn("用户会话 {} 已存在于房间 {} 的监听者列表中", userSession.getId(), roomId);
        }

        // 建立反向映射
        sessionToRoomMap.put(userSession, roomId);
        log.info("已建立用户会话映射");
        // 如果B站连接未建立或已断开，则重新连接
        if (!client.isOpen()) {
            log.info("正在重新连接房间 {} 的B站连接", roomId);
            client.connect();
            // 连接成功后广播消息
            broadcastToUsers(roomId, "房间 " + roomId + " 已连接");
        } else {
            log.info("房间 {} 的B站连接已建立", roomId);
            // 如果已经连接，也广播消息
            broadcastToUsers(roomId, "房间 " + roomId + " 已连接");
        }
    }

    // 供BilibiliRoomWebSocketHandler调用，用于清理用户会话映射
    public void removeUserSessionMapping(WebSocketSession session) {
        sessionToRoomMap.remove(session);
        System.out.println("已清理用户会话映射");
    }

    public void removeUserSession(WebSocketSession session) {
        log.info("正在清理用户会话 {}", session.getId());
        Integer roomId = sessionToRoomMap.remove(session);
        if (roomId != null) {
            log.info("正在清理房间 {} 的用户会话映射", roomId);
            CopyOnWriteArraySet<WebSocketSession> sessions = roomUserSessions.get(roomId);
            if (sessions != null) {
                boolean removed = sessions.remove(session);
                log.info("已清理房间 {} 的用户会话映射: {}", roomId, removed);
                // 如果该房间没有用户了，关闭B站连接
                if (sessions.isEmpty()) {
                    log.info("房间 {} 没有用户，正在关闭B站连接", roomId);
                    BilibiliClient client = roomClients.remove(roomId);
                    if (client != null) {
                        client.disconnect();
                    }
                    roomUserSessions.remove(roomId);
                    log.info("已关闭B站连接");
                }
            } else {
                log.warn("房间 {} 的映射不存在", roomId);
            }
        } else {
            log.warn("用户会话 {} 的映射不存在", session.getId());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("已连接");
//        session.sendMessage(new TextMessage("连接已建立，请发送'connect:房间号:cookie'来连接B站直播间"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            if (payload.startsWith("connect:")) {
                // 解析格式: connect:房间号:cookie
                String[] parts = payload.split(":", 3);
                if (parts.length == 3) {
                    try {
                        int roomId = Integer.parseInt(parts[1]);
                        String cookie = parts[2];
                        if (cookie.isEmpty()) {
                            cookie = redisTemplate.opsForValue().get("cookie");
                        }

                        // 连接到指定的B站直播间
                        connectToRoom(roomId, cookie, session);
                        session.sendMessage(new TextMessage("正在连接到B站直播间 " + roomId + "..."));
                    } catch (Exception e) {
                        session.sendMessage(new TextMessage("连接失败: " + e.getMessage()));
                    }
                } else {
                    session.sendMessage(new TextMessage("格式错误，请使用 'connect:房间号:cookie'"));
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("用户WebSocket传输错误: {}", exception.getMessage());
        exception.printStackTrace();
        // 发生传输错误时也移除会话
        removeUserSession(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

        log.info("用户WebSocket连接已关闭: {}", closeStatus.getReason());
        // 连接关闭时也移除会话
        removeUserSession(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void sendHeartbeats() {
//        log.info("开始发送心跳包");
        // 向所有已连接的B站WebSocket会话发送心跳包
        for (BilibiliClient client : roomClients.values()) {
            if (client.isOpen()) {
                client.sendHeartbeat();
            }
        }
    }
    // 在BilibiliWebSocketService类中添加以下方法
    private void broadcastToUsers(int roomId, String message) {
        CopyOnWriteArraySet<WebSocketSession> sessions = roomUserSessions.get(roomId);
        if (sessions != null && !sessions.isEmpty()) {
            CopyOnWriteArraySet<WebSocketSession> sessionsToRemove = new CopyOnWriteArraySet<>();

            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        synchronized (session) {
                            session.sendMessage(new TextMessage(message));
                            log.info("向用户客户端 {} 发送消息成功: {}", session.getId(), message);
                        }
                    } else {
                        log.info("用户客户端 {} 已关闭", session.getId());
                        sessionsToRemove.add(session);
                    }
                } catch (Exception e) {
                    log.error("向用户客户端 {} 发送消息失败: {}", session.getId(), e.getMessage());
                    sessionsToRemove.add(session);
                }
            }

            // 清理已关闭的会话
            if (!sessionsToRemove.isEmpty()) {
                log.info("清理 " + sessionsToRemove.size() + " 个已关闭的会话");
                for (WebSocketSession session : sessionsToRemove) {
                    sessions.remove(session);
                    sessionToRoomMap.remove(session);
                }
            }
        }
    }

}
