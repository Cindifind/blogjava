package com.example.demo.bilibliWebsocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class UserWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private BilibiliWebSocketService bilibiliService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
//    public static String ADMIN_COOKIE="buvid3=40854B6E-BED2-25EF-3E23-6902F2486CEC69685infoc; b_nut=1761727469; buvid_fp=9474d9ef00556bc6ff45939adf0ecea7; _uuid=14DFCC97-63C8-EDBE-01E5-5F355083921408333infoc; theme-tip-show=SHOWED; theme-avatar-tip-show=SHOWED; theme-switch-show=SHOWED; LIVE_BUVID=AUTO9517617276473025; buvid4=E8D03DC7-C96B-6FD8-BA5A-32FECF3CDCD689276-025092712-Q3vP71kXNmdvr+Jg/2U//wJGGY3QozAWtQAcSdG8P6fFCI86dCLrtbFfiDpGGrEy; rpdid=|(kYRk|YmYk|0J'u~Yu~k)RmJ; CURRENT_QUALITY=32; enable_web_push=DISABLE; bp_t_offset_437366177=1129351291022082048; DedeUserID=3493112141842660; DedeUserID__ckMd5=fb2ce06254629469; ogv_device_support_hdr=0; CURRENT_FNVAL=2000; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NjI0NzY3ODAsImlhdCI6MTc2MjIxNzUyMCwicGx0IjotMX0.2fI8583I2nf6k49pqdVcGk_WDp26cG7DSd2CYUEhFXo; bili_ticket_expires=1762476720; SESSDATA=be3ce26e%2C1777769581%2C12ebb%2Ab2CjBz2DjB4H5DWW88m3E6Yukxy9-OGEbgdRpJ7S0Rpet5aM4Z48TdfHNAGYbcZzpb10ESVnE4V09GczV4ajFBUUJPN2F0WmwxSmtIc0lfcXJKN3dZaUM5SUk2dmYweEp6QXBmeXZvVmgxMWhNQ0ZVZXRxcjJ0T29NT0l2SHQ1SEw5blNDbU0xUFNRIIEC; bili_jct=22b5635581dd81ffcafee276f3004f88; sid=79yq8r8i; bp_t_offset_3493112141842660=1131370483996950528; b_lsid=D952A836_19A515EE4BA; home_feed_column=5; browser_resolution=1699-852; PVID=3";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.sendMessage(new TextMessage("连接已建立，请发送'connect:房间号:cookie'来连接B站直播间"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (payload.startsWith("connect:")) {
            // 解析格式: connect:房间号:cookie
            String[] parts = payload.split(":", 3);
            if (parts.length == 3) {
                try {
                    int roomId = Integer.parseInt(parts[1]);
                    String cookie = parts[2];
                    if (cookie.isEmpty()) {
                        cookie = redisTemplate.opsForValue().get("cookie");
//                        cookie = ADMIN_COOKIE;
                    }

                    // 连接到指定的B站直播间
                    bilibiliService.connectToRoom(roomId, cookie, session);
                    //session.sendMessage(new TextMessage("正在连接到B站直播间 " + roomId + "..."));
                } catch (Exception e) {
                    session.sendMessage(new TextMessage("连接失败: " + e.getMessage()));
                }
            } else {
                session.sendMessage(new TextMessage("格式错误，请使用 'connect:房间号:cookie'"));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("用户连接已关闭: " + status.getReason());
        bilibiliService.removeUserSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("用户WebSocket传输错误: " + exception.getMessage());
        bilibiliService.removeUserSession(session);
    }
}
