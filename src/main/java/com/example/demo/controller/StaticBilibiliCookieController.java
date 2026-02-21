package com.example.demo.controller;

import org.example.text.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StaticBilibiliCookieController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Client(address = "/admin/staticBilibiliCookie", name = "staticBilibiliCookie")
    @GetMapping("/admin/staticBilibiliCookie")
    public ResponseEntity<Map<String, Object>> getStaticBilibiliCookie(@RequestParam String cookie) {
        redisTemplate.opsForValue().set("cookie", cookie);
        return ResponseEntity.ok(Map.of("message", "Cookie已更新"));
    }
}