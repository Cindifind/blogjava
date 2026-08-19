package com.example.demo.controller;

import com.example.demo.server.LLMApiServe;
import org.example.text.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Client(address = "/live2Dllm",name = "live2Dllm")
    public ResponseEntity<Map<String, Object>> getLLMResponse(@RequestBody String prompt,long timestamp ,String nonce) {
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
    @Client(address = "/live2DVoice",name = "live2DVoice")
    public ResponseEntity<byte[]> getLLMVoiceResponse(@RequestBody String prompt) {
        byte[] voiceFile = llmApiServe.getVoice(prompt);
        if (voiceFile == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"voice.wav\"")
                .body(voiceFile);
    }
}
