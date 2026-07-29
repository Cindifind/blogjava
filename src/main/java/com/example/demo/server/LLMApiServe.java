package com.example.demo.server;

import com.example.demo.util.LLMApiUtil;
import org.springframework.stereotype.Service;

@Service
public class LLMApiServe {
    private final LLMApiUtil llmApiUtil;
    public LLMApiServe(){
        this.llmApiUtil = new LLMApiUtil();
    }
    public String getLLMResponse(String prompt) {
        return llmApiUtil.getLLMResponse(prompt);
    }
}
