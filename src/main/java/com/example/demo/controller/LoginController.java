package com.example.demo.controller;

import com.example.demo.auth.mapper.UserInfoMapper;
import com.example.demo.auth.model.UserInfo;
import com.example.demo.auth.util.GetSh256;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.example.text.client.Client;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController

public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    UserInfoMapper userInfoMapper;
    @Client(address = "/login",name = "login")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String email, @RequestParam String password){
        String beforeToken = email + password;
        String afterToken = GetSh256.getSha256Hash(beforeToken);
        UserInfo userInfo = userInfoMapper.getUserInfoByToken(afterToken);
        if (userInfo == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户不存在或账号密码不正确");
            return ResponseEntity.status(401).body(response);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "登录成功");
        response.put("data", userInfo);
        HttpResponse<String> name = Unirest.get("https://api.xinyew.cn/api/qqtxnc?qq=" + userInfo.getEmail().replaceAll("@qq.com", ""))
                .header("Accept", "application/vnd.github.v3+json")
                .asString();
        try {
            JSONObject QQname = new JSONObject(name.getBody());
            QQname = QQname.getJSONObject("data");
            userInfo.setImgUrl(QQname.getString("avatar"));
            userInfo.setName(QQname.getString("name"));
        }catch (Exception e){
            log.info("非qq邮箱登录");
        }
        return ResponseEntity.ok(response);
    }
}
