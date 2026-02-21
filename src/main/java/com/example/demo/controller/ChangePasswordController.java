package com.example.demo.controller;

import com.example.demo.auth.mapper.UserInfoMapper;
import com.example.demo.auth.server.VerificationCodeService;
import com.example.demo.auth.util.GetSh256;
import jakarta.servlet.http.HttpServletRequest;
import org.example.text.client.Client;
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
public class ChangePasswordController {
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Client(address = "/api/forgetPassword",name = "forgetPassword")
    @PostMapping("/forgetPassword")
    public ResponseEntity<Map<String, Object>> forgetPassword(@RequestParam String email, HttpServletRequest request) {
        // 验证邮箱是否存在
        if (userInfoMapper.getEmail(email) == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "邮箱不存在");
            return ResponseEntity.ok(response);
        }

        // 发送验证码
        // 直接使用服务层返回的响应
        return verificationCodeService.sendVerificationCode(email, request);
    }
    @Client(address = "/api/changePassword",name = "changePassword")
    @PostMapping("/changePassword")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestParam String email,
                                                              @RequestParam String code,
                                                              @RequestParam String password,
                                                              HttpServletRequest request) {
        // 验证验证码
        ResponseEntity<Map<String, Object>> verifyResult = verificationCodeService.verifyCode(email, code, request);

        // 如果验证失败，直接返回服务层的响应
        if (!verifyResult.getStatusCode().is2xxSuccessful()) {
            return verifyResult;
        }

        // 验证成功，更新密码
        try {
            userInfoMapper.updateUserInfoPassword(email, GetSh256.getSha256Hash(email + password));
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "密码修改成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "密码修改失败");
            return ResponseEntity.status(500).body(response);
        }
    }
}
