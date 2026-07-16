package com.example.demo.controller;

import com.example.demo.auth.mapper.UserInfoMapper;
import com.example.demo.auth.model.UserInfo;
import com.example.demo.auth.util.Argon2Util;
import com.example.demo.auth.util.GetSh256;
import com.example.demo.server.UserInfoServer;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.example.text.client.Client;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController

public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserInfoServer userInfoServer;

    @Client(address = "/login", name = "login")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String emailHash, @RequestParam String password) {
        return ResponseEntity.ok(userInfoServer.login(emailHash, password));
    }

    @Client(address = "/getsalt", name = "getsalt")
    @GetMapping("/getsalt")
    public ResponseEntity<Map<String, Object>> getSalt(@RequestParam String email, @RequestParam String hash) {
        return ResponseEntity.ok(userInfoServer.getSalt(email, hash));
    }

}
