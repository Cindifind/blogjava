package com.example.demo.controller;

import com.example.demo.model.H5Stats;
import com.example.demo.server.H5StatService;
import org.example.text.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class H5StatsController {
    @Autowired
    private H5StatService h5StatService;
    @Client(address = "/h5Stats",name = "h5Stats")
    @GetMapping("/h5Stats")
    public ResponseEntity<Map<String, Object>> getH5Stats(){
        Map<String, Object> result = new HashMap<>();
        result.put("status", "200");
        result.put("message", "获取成功");
        result.put("data", h5StatService.getAllH5Stats());
        return ResponseEntity.ok(result);
    }
    @Client(address = "/admin/insertH5Url",name = "insertH5Url")
    @PostMapping("/admin/insertH5Url")
    public ResponseEntity<Map<String, Object>> insertH5Url(@RequestBody Map<String, Object> requestBody){
        Map<String, Object> result = new HashMap<>();

        try {
            // 从Map中提取H5Stats对象
            Map<String, Object> h5StatsMap = (Map<String, Object>) requestBody.get("h5Stats");
            H5Stats h5Stats = h5StatService.createH5StatsFromMap(h5StatsMap);

            // 提取API列表
            List<String> api = (List<String>) requestBody.get("apiList");

            if (h5StatService.insertH5Stats(h5Stats, api) == 1){
                result.put("status", "200");
                result.put("message", "插入成功");
            }else {
                result.put("status", "500");
                result.put("message", "插入失败");
            }
        } catch (Exception e) {
            result.put("status", "500");
            result.put("message", "参数格式错误: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @Client(address = "/admin/updateH5Url",name = "updateH5Url")
    @PostMapping("/admin/updateH5Url")
    public ResponseEntity<Map<String, Object>> updateH5Url(@RequestBody Map<String, Object> requestBody){
        Map<String, Object> result = new HashMap<>();

        try {
            // 从Map中提取各个参数
            String url = (String) requestBody.get("url");
            List<String> api = (List<String>) requestBody.get("apiList");

            // 提取H5Stats对象
            Map<String, Object> h5StatsMap = (Map<String, Object>) requestBody.get("h5Stats");
            H5Stats h5Stats = h5StatService.createH5StatsFromMap(h5StatsMap);

            if (h5StatService.updateH5StatsApi(url, api, h5Stats) == 1){
                result.put("status", "200");
                result.put("message", "更新成功");
            }else {
                result.put("status", "500");
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            result.put("status", "500");
            result.put("message", "参数格式错误: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @Client(address = "/admin/deleteH5Url",name = "deleteH5Url")
    @PostMapping("/admin/deleteH5Url")
    public ResponseEntity<Map<String, Object>> deleteH5Url(@RequestParam String url){
        Map<String, Object> result = new HashMap<>();
        if (h5StatService.deleteH5Stats(url) == 1){
            result.put("status", "200");
            result.put("message", "删除成功");
        }else {
            result.put("status", "500");
            result.put("message", "删除失败");
        }
        return ResponseEntity.ok(result);
    }

    @Client(address = "/apiInfo",name = "apiInfo")
    @GetMapping("/apiInfo")
    public ResponseEntity<Map<String, Object>> getApiInfo(@RequestParam String url){
        Map<String, Object> result = new HashMap<>();
        result.put("status", "200");
        result.put("message", "获取成功");
        result.put("data", h5StatService.getH5StatsApi(url));
        return ResponseEntity.ok(result);
    }
}