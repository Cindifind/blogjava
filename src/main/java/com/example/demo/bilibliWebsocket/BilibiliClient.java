package com.example.demo.bilibliWebsocket;

import com.example.demo.bilibliWebsocket.map.Address;
import com.example.demo.bilibliWebsocket.map.Datacenter;
import com.example.demo.bilibliWebsocket.tool.BiliBiliTool;
import com.example.demo.bilibliWebsocket.map.Package;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.example.demo.bilibliWebsocket.Json.JSON;

public class BilibiliClient {
    private static final Logger log = LoggerFactory.getLogger(BilibiliClient.class);
    private WebSocketSession session;
    private final int roomId;
    private final String cookie;
    private final BilibiliRoomWebSocketHandler handler;

    public BilibiliClient(int roomId, String cookie, BilibiliWebSocketService service,
                          CopyOnWriteArraySet<WebSocketSession> userSessions) {
        this.roomId = roomId;
        this.cookie = cookie;
        this.handler = new BilibiliRoomWebSocketHandler(roomId, service, userSessions);
    }

    public void connect() throws Exception {
        String url = BiliBiliTool.buildDanmuInfoUrl(roomId);
        //使用Util.getRoomAddressJson()方法获取地址，并使用readValue方法将JSON转成Address对象
        Address address = JSON.readValue(Util.getRoomAddressJson(url, roomId, cookie), Address.class);
        //获取地址主要信息
        int port = address.getData().getHostList().get(0).getWssPort();
        String host = address.getData().getHostList().get(0).getHost();
        String token = address.getData().getToken();
        Map<String, String> map = Datacenter.getCookieMap(cookie);
        //连接并发送心跳包
        URI uri = new URI("wss://" + host + ":" + port + "/sub");
        Package pkg = new Package(
                Long.parseLong(map.get("DedeUserID")),
                roomId,
                2,
                "buvid3="+map.get("buvid3"),
                "web",
                2,
                token);
        //创建鉴权包
        byte[] dataPackage = Package.createDataPackage(7, JSON.writeValueAsString(pkg).getBytes(StandardCharsets.UTF_8));
        //创建客户端
        WebSocketClient client = new StandardWebSocketClient();

        // 使用execute方法连接
        CompletableFuture<WebSocketSession> future = client.execute(handler, new WebSocketHttpHeaders(), uri);
        this.session = future.get();

        // 发送认证包
        if (session.isOpen()) {
            session.sendMessage(new org.springframework.web.socket.BinaryMessage(dataPackage));
            log.info("房间 {} 鉴权成功", roomId);
        }
    }

    public void disconnect() {
        try {
            log.info("正在关闭房间 {} 的B站连接", roomId);
            if (session != null && session.isOpen()) {
                session.close();
                log.info("已关闭房间 {} 的B站连接", roomId);
            }
        } catch (Exception e) {
            log.error("关闭房间 {} 的B站连接时出错: {}", roomId, e.getMessage());
        }
    }

    public boolean isOpen() {
        return session != null && session.isOpen();
    }

    public void sendHeartbeat() {
        try {
            if (session != null && session.isOpen()) {
                // 创建心跳包（协议类型2，空包体）
                byte[] heartbeat = Package.createDataPackage(2, new byte[0]);
                session.sendMessage(new org.springframework.web.socket.BinaryMessage(heartbeat));
                log.info("房间 {} 发送心跳包", roomId);
            }
        } catch (Exception e) {
            log.error("发送心跳包时出错: {}", e.getMessage());
        }
    }

    public int getRoomId() {
        return roomId;
    }

}
