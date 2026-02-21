package com.example.demo.controller;

import com.example.demo.auth.mapper.UserInfoMapper;
import com.example.demo.auth.model.UserInfo;
import com.example.demo.auth.server.RegisterServer;
import com.example.demo.auth.server.VerificationCodeService;
import com.example.demo.auth.util.GetSh256;
import jakarta.servlet.http.HttpServletRequest;
import org.example.text.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegisterController {
    private static final Logger log = LoggerFactory.getLogger(RegisterController.class);
    @Autowired
    private RegisterServer registerServer;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Client(address = "/api/register",name = "register")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestParam String email,
                                                        @RequestParam String password,
                                                        @RequestParam String code,
                                                        @RequestParam int grade,
                                                        HttpServletRequest request) {
        try {
            // 检查邮箱是否已存在
            if (userInfoMapper.getEmail(email) != null) {
                return createErrorResponse("邮箱已存在");
            }

            // 创建用户信息
            String beforeToken = email + password;
            String afterToken = GetSh256.getSha256Hash(beforeToken);
            UserInfo userInfo = new UserInfo();
            userInfo.setEmail(email);
            userInfo.setToken(afterToken);
            userInfo.setGrade(grade);

            // 调用注册服务
            return registerServer.registerUser(userInfo, code, request);
        } catch (Exception e) {
            log.error("注册失败", e);
            return createErrorResponse("注册失败: " + e.getMessage());
        }
    }
    @Client(address = "/api/register/send/code",name = "sendCode")
    @PostMapping("/register/send/code")
    public ResponseEntity<Map<String, Object>> sendCode(@RequestParam String email, HttpServletRequest request) {
        try {
            // 检查邮箱是否已存在
            if (userInfoMapper.getEmail(email) != null) {
                return createErrorResponse("邮箱已存在");
            }

            // 发送验证码
            // 直接返回服务层的响应结果
            return verificationCodeService.sendVerificationCode(email, request);
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            return createErrorResponse("发送验证码失败: " + e.getMessage());
        }
    }
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", message);
        response.put("data", null);
        return ResponseEntity.ok(response);
    }
}
