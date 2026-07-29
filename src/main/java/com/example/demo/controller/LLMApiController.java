package com.example.demo.controller;

import com.example.demo.server.LLMApiServe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LLMApiController {
    @Autowired
    private LLMApiServe llmApiServe;
    private static final Logger log = LoggerFactory.getLogger(LLMApiController.class);
    @PostMapping("/live2Dllm")
    public ResponseEntity<Map<String, Object>> getLLMResponse(String prompt) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("message", llmApiServe.getLLMResponse(prompt));
            log.info("LLM服务调用成功");
        }catch (Exception e){
            response.put("message", "LLM服务异常");
            log.error("LLM服务异常", e);
        }
        response.put("status", "200");
        return  ResponseEntity.ok(response);
    }
}
