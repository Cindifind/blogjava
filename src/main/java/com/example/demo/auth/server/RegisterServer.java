package com.example.demo.auth.server;

import com.example.demo.auth.mapper.UserInfoMapper;
import com.example.demo.auth.model.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterServer {
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private UserInfoMapper userInfoMapper;

    public ResponseEntity<Map<String, Object>> registerUser(UserInfo userInfo, String code, HttpServletRequest request) {
        // 验证验证码
        ResponseEntity<Map<String, Object>> verifyResult = verificationCodeService.verifyCode(userInfo.getEmail(), code, request);

        // 如果验证码验证失败，直接返回验证结果
        if (!verifyResult.getStatusCode().is2xxSuccessful()) {
            return verifyResult;
        }

        try {
            // 验证成功，执行注册逻辑
            userInfo.setIsEnable(userInfo.getGrade() == 1);
            userInfoMapper.insertUserInfo(userInfo);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "注册成功");
            response.put("data", userInfo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "注册失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
