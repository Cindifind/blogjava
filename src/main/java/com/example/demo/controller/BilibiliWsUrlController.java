package com.example.demo.controller;

import com.example.demo.bilibliWebsocket.sendmessage.sendMessageBilibili;
import com.example.demo.bilibliWebsocket.tool.BiliBiliTool;
import com.example.demo.server.ApiUrlServer;
import jakarta.servlet.http.HttpServletRequest;
import org.example.text.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Client(address = "/api/bilibili", name = "bilibili")
@RestController
@RequestMapping("/api/bilibili")
public class BilibiliWsUrlController {
    @Autowired
    private ApiUrlServer apiUrlServer;

    @PostMapping("/sendMessage")
    public ResponseEntity<String> sendMessage(
            @RequestParam String roomId,
            @RequestParam String cookie,
            @RequestParam String message,
            HttpServletRequest request) {
        // 验证必填参数
        if (roomId == null || roomId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("房间号不能为空");
        }
        if (cookie == null || cookie.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Cookie不能为空");
        }

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("消息内容不能为空");
        }

        try {
            sendMessageBilibili sendMessageBilibili = new sendMessageBilibili();
            String result = sendMessageBilibili.sendMessage(roomId, cookie, message);
            apiUrlServer.UpDataaApiState(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("发送消息失败: " + e.getMessage());
        }
    }

    @GetMapping("/DanmuInfoUrl")
    public ResponseEntity<String> getDanmuInfoUrl(@RequestParam long roomId, HttpServletRequest request) {
        try {
            String result = BiliBiliTool.buildDanmuInfoUrl(roomId);
            apiUrlServer.UpDataaApiState(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("获取弹幕信息URL失败: " + e.getMessage());
        }
    }

    @PostMapping("/getWbiSign")
    public ResponseEntity<String> getWbiSign(@RequestBody Map<String, String> params, HttpServletRequest request) {
        try {
            Map<String, String> result = BiliBiliTool.wbiSign(params);
            apiUrlServer.UpDataaApiState(request);
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("获取WBI签名失败: " + e.getMessage());
        }
    }
}

