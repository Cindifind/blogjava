package com.example.demo.server;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ElysiaVoiceServer {
    private static final Logger log = LoggerFactory.getLogger(ElysiaVoiceServer.class);
    
    public byte[] getVoice(String text) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("model", "FunAudioLLM/CosyVoice2-0.5B");
            String elysia = "speech:Elysia:sbdn8crxf9:mststxysjyefjprcoino";
            jsonObject.put("voice", elysia);
            jsonObject.put("input", text);
            jsonObject.put("response_format", "mp3");
            
            // 使用Unirest构建请求
            String elysia_key = "sk-mkgdltfgwnpetbuvrpnszqepevcxpusoghylastsmnrxkqvy";
            String URL = "https://api.siliconflow.cn";
            HttpResponse<byte[]> response = Unirest.post(URL + "/v1/audio/speech")
                    .header("Authorization", "Bearer " + elysia_key)
                    .header("Content-Type", "application/json")
                    .body(jsonObject.toString())
                    .asBytes();
            
            // 检查HTTP响应状态码
            if (response.getStatus() != 200) {
                log.error("获取语音信息失败");
                return null;
            }
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("获取语音信息异常失败");
            return null;
        }
    }
}