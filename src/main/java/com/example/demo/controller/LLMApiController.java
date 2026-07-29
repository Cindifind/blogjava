package com.example.demo.controller;

import com.example.demo.server.LLMApiServe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LLMApiController {
    @Autowired
    private LLMApiServe llmApiServe;
    private static final Logger log = LoggerFactory.getLogger(LLMApiController.class);
    @PostMapping("/live2Dllm")
    public ResponseEntity<Map<String, Object>> getLLMResponse(@RequestBody String prompt) {
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
    @PostMapping("/live2DVoice")
    public ResponseEntity<Resource> getLLMVoiceResponse(@RequestBody String prompt) {
        File voiceFile = llmApiServe.getVoice(prompt);
        Resource resource = new FileSystemResource(voiceFile);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + voiceFile.getName() + "\"")
                .body(resource);
    }
}
