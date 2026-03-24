package com.example.demo.controller;

import com.example.demo.auth.mapper.UserInfoMapper;
import com.example.demo.auth.model.UserInfo;
import com.example.demo.auth.util.Argon2Util;
import com.example.demo.auth.util.GetSh256;
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
    private UserInfoMapper userInfoMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Client(address = "/login", name = "login")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String emailHash, @RequestParam String password) {
        String email = redisTemplate.opsForValue().get(emailHash);
        UserInfo userInfo = userInfoMapper.getUserInfoByToken(password);
        if (userInfo == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户不存在或账号密码不正确");
            return ResponseEntity.status(401).body(response);
        }
        if (!userInfo.getEmail().equals(email)) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户不存在或账号密码不正确");
            return ResponseEntity.status(401).body(response);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "登录成功");
        response.put("data", userInfo);
        HttpResponse<String> name = Unirest.get("https://users.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins=" + userInfo.getEmail().replaceAll("@qq.com", "")).header("Accept", "application/vnd.github.v3+json").asString();
        try {
            JSONObject QQname = new JSONObject(name.getBody().replaceAll(".*\\((.*)\\)", "$1"));
            JSONArray QQInfoArray = QQname.getJSONArray(userInfo.getEmail().replaceAll("@qq.com", ""));
            userInfo.setImgUrl(QQInfoArray.getString(0));
            userInfo.setName(QQInfoArray.getString(6));
        } catch (Exception e) {
            log.info("非qq邮箱登录");
        }
        return ResponseEntity.ok(response);
    }

    @Client(address = "/getsalt", name = "getsalt")
    @GetMapping("/getsalt")
    public ResponseEntity<Map<String, Object>> getSalt(@RequestParam String email, @RequestParam String hash) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取盐成功");
        String isTrueEmail = Argon2Util.argon2Hash(email, Argon2Util.SALT);
        if (!isTrueEmail.equals(hash)) {
            response.put("data", Argon2Util.generateRandomSalt());
            return ResponseEntity.ok(response);
        }
        String salt = userInfoMapper.getSalt(email);
        if (salt == null) {
            salt = Argon2Util.generateRandomSalt();
        } else {
            redisTemplate.opsForValue().set(hash, email, 5, TimeUnit.SECONDS);
        }
        response.put("data", salt);
        return ResponseEntity.ok(response);
    }

}
