package com.example.demo.controller;

import com.example.demo.mapper.ApiStatsMapper;
import com.example.demo.model.ApiStats;
import jakarta.servlet.http.HttpServletResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.example.text.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ApiController {
    @Autowired
    private ApiStatsMapper apiStatsMapper;

    @Client(address = "/apiStats", name = "apiStats")
    @GetMapping("/apiStats")
    public List<ApiStats> getApiStats() {
        return apiStatsMapper.getAllApiStats();
    }

    @Client(address = "/admin/insertApiUrl", name = "insertApiUrl")
    @PostMapping("/admin/insertApiUrl")
    public ResponseEntity<Map<String, Object>> insertApiUrl(String url) {
        apiStatsMapper.insertApiStats(url);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "200");
        result.put("message", "插入成功");
        return ResponseEntity.ok(result);
    }

    @Client(address = "/admin/updateApiUrl", name = "updateApiUrl")
    @PostMapping("/admin/updateApiUrl")
    public ResponseEntity<Map<String, Object>> updateApiUrl(String url, String description, String java, String python, String javascript) {
        apiStatsMapper.updateApiStatsDescription(url, description, java, python, javascript);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "200");
        result.put("message", "更新成功");
        return ResponseEntity.ok(result);
    }
    @Client(address = "/admin/deleteApi", name = "deleteApi")
    @PostMapping("/admin/deleteApi")
    public ResponseEntity<Map<String, Object>> deleteApi(String url) {
        apiStatsMapper.deleteApiStats(url);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "200");
        result.put("message", "删除成功");
        return ResponseEntity.ok(result);
    }
    @Client(address = "/apiList", name = "apiList")
    @PostMapping("/apiList")
    public ResponseEntity<Map<String, Object>> getApiList(@RequestBody List<String> apiNameList) {
        Map<String, Object> result = new HashMap<>();
        result.put("data", apiStatsMapper.getApiList(apiNameList));
        result.put("status", "200");
        result.put("message", "获取成功");
        return ResponseEntity.ok(result);
    }
    @GetMapping("/api/proxy-video")
    public void proxyVideo(@RequestParam String url, HttpServletResponse response) throws Exception {
        // 解码URL参数
        String decodedUrl = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8);

        HttpResponse<byte[]> videoResponse = Unirest.get(decodedUrl)
                .header("Referer", "https://www.bilibili.com")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .asBytes();

        response.setContentType("video/mp4");
        response.getOutputStream().write(videoResponse.getBody());
    }
}
