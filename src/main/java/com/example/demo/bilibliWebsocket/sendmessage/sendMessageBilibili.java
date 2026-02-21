package com.example.demo.bilibliWebsocket.sendmessage;

import com.example.demo.bilibliWebsocket.UserWebSocketHandler;
import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class sendMessageBilibili {

    public String sendMessage(String roomId, String cookie, String message) throws Exception {
        // 从cookie中提取csrf token
        String csrfToken = extractCsrfToken(cookie);

        // 获取当前时间戳（秒级）
        long timestamp = System.currentTimeMillis() / 1000;

        // 使用Unirest发送POST请求
        HttpResponse<String> response = Unirest.post("https://api.live.bilibili.com/msg/send")
                .header("User-Agent", "Mozilla/5.0")
                .header("Cookie", cookie)
                .header("Origin", "https://live.bilibili.com")
                .header("Referer", "https://live.bilibili.com/" + roomId)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("color", "16777215")
                .field("fontsize", "25")
                .field("mode", "1")
                .field("msg", message)
                .field("rnd", String.valueOf(timestamp))
                .field("roomid", roomId)
                .field("csrf", csrfToken)
                .field("csrf_token", csrfToken)
                .asString();

        // 输出响应结果
        return "Send danmu API response: " + response.getBody();
    }

    private String extractCsrfToken(String cookies) {
        Pattern pattern = Pattern.compile("bili_jct=([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(cookies);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("无法从cookies中提取CSRF token");
        }
    }
}
