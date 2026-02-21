package com.example.demo.bilibliWebsocket;

import cn.hutool.core.util.ZipUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArraySet;

public class BilibiliRoomWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(BilibiliRoomWebSocketHandler.class);
    private final int roomId;
    private final BilibiliWebSocketService service;
    private final CopyOnWriteArraySet<WebSocketSession> userSessions;

    public BilibiliRoomWebSocketHandler(int roomId, BilibiliWebSocketService service,
                                        CopyOnWriteArraySet<WebSocketSession> userSessions) {
        this.roomId = roomId;
        this.service = service;
        this.userSessions = userSessions;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("房间 {} 已连接", roomId);
//        broadcastToUsers("房间 " + roomId + " 已连接");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof BinaryMessage) {
            ByteBuffer bytes = ((BinaryMessage) message).getPayload();
            String content = unpack(bytes);
            if (content != null && content.charAt(0) == '{') {
                JSONObject jsonObject = new JSONObject(content);
                if (jsonObject.has("info")) {
                    JSONArray array = jsonObject.getJSONArray("info");
                    String username = array.getJSONArray(2).get(1).toString();
                    String danmu = array.get(1).toString();
                    // 广播弹幕给所有监听此房间的用户
                    broadcastToUsers("[弹幕]" + username + "说: " + danmu);
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("房间 {} B站WebSocket传输错误: {}", roomId, exception.getMessage());
        exception.printStackTrace();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("房间 {} 已关闭", roomId);
        // 当房间连接关闭时，通知所有用户客户端
        broadcastToUsers("房间 " + roomId + " 连接已断开，可能是由于cookie不正确导致的。请联系网站管理者或自行填写正确的cookie!");
        
        // 主动断开所有用户连接
        closeAllUserConnections();
    }

    /**
     * 主动关闭所有连接到此房间的用户WebSocket连接
     */
    private void closeAllUserConnections() {
        for (WebSocketSession userSession : userSessions) {
            try {
                if (userSession.isOpen()) {
                    // 主动关闭与用户端的WebSocket连接
                    userSession.close(CloseStatus.GOING_AWAY.withReason("B站直播间连接已断开"));
                }
                // 从服务中移除用户会话
                service.removeUserSession(userSession);
            } catch (IOException e) {
                log.error("关闭用户会话 {} 时出错: {}", userSession.getId(), e.getMessage());
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void broadcastToUsers(String message) {
        log.info("房间 {} 广播消息: {}", roomId, message);

        CopyOnWriteArraySet<WebSocketSession> sessionsToRemove = new CopyOnWriteArraySet<>();

        for (WebSocketSession session : userSessions) {
            try {
                if (session.isOpen()) {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(message));
                        log.info("向用户客户端 {} 发送消息成功", session.getId());
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
                userSessions.remove(session);
                service.removeUserSessionMapping(session);
            }
        }
    }

    public static String unpack(ByteBuffer byteBuffer) {
        try {
            if (byteBuffer.remaining() < 16) {
                log.warn("数据包不完整，需要等待更多数据");
                return null;
            }
            int packageLen = byteBuffer.getInt();
            short headLength = byteBuffer.getShort();
            short packageVer = byteBuffer.getShort();
            int optCode = byteBuffer.getInt();
            int sequence = byteBuffer.getInt();

            if (2 == optCode) {
                log.info("这是服务器心跳回复");
            }

            byte[] contentBytes = new byte[packageLen - headLength];
            byteBuffer.get(contentBytes);

            // 如果是zip包就进行解包
            if (2 == packageVer) {
                return unpack(ByteBuffer.wrap(ZipUtil.unZlib(contentBytes)));
            }

            String content = new String(contentBytes, StandardCharsets.UTF_8);

            if (5 == optCode) {
                return content;
            }

            if (byteBuffer.position() < byteBuffer.limit()) {
                return unpack(byteBuffer);
            }

            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}