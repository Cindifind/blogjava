package com.example.demo.controller;

import com.example.demo.model.SeoPage;
import com.example.demo.server.SeoPageServer;
import jakarta.servlet.http.HttpServletRequest;
import org.example.text.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SeoPageController {
    @Autowired
    private SeoPageServer seoPageServer;
    @GetMapping("/seoPage")
    @Client(name = "seoPage", address = "/seoPage")
    public ResponseEntity<String> getSeoPage(String urlPath) {
        return ResponseEntity.ok(seoPageServer.getSeoPageByUrlPathToFullHtml(urlPath));
    }
    @GetMapping("/sitemap")
    @Client(name = "sitemap", address = "/sitemap")
    public ResponseEntity<String> getSeoPageSitemapXml(String urlPath) {
        return ResponseEntity.ok(seoPageServer.getSeoPageByUrlPathToSitemapXml(urlPath));
    }
    @PostMapping("/user/seoPageInsert")
    @Client(name = "seoPageInsert", address = "/user/seoPageInsert")
    public ResponseEntity<Map<String,Object>> insertSeoPage(@RequestBody SeoPage seoPage, HttpServletRequest request) {
        int result = seoPageServer.insertSeoPage(seoPage, request);
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("result", result);
        resultData.put("seoPage", seoPage);
        resultData.put("message", "Insert successful");
        return ResponseEntity.ok(resultData);
    }
    @PostMapping("/user/seoPageUpdate")
    @Client(name = "seoPageUpdate", address = "/user/seoPageUpdate")
    public ResponseEntity<Map<String,Object>> updateSeoPage(@RequestBody SeoPage seoPage,HttpServletRequest request) {
        int result = seoPageServer.updateSeoPage(seoPage, request);
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("result", result);
        resultData.put("seoPage", result == 0?"fail":seoPage);
        resultData.put("message", result == 0?"fail":"Update successful");
        return ResponseEntity.ok(resultData);
    }
    @PostMapping("/user/selectSeo")
    @Client(name = "selectSeo", address = "/user/selectSeo")
    public ResponseEntity<List<SeoPage>> selectSeoPageByUserEmail(HttpServletRequest request) {
        return ResponseEntity.ok(seoPageServer.selectByUserEmail(request));
    }
    @GetMapping("/user/seoPageDelete")
    @Client(name = "seoPageDelete", address = "/user/seoPageDelete")
    public ResponseEntity<Map<String,Object>> deleteSeoPage(@RequestParam String urlPath, HttpServletRequest request) {
        int result = seoPageServer.deleteSeoPageByUrlPath(urlPath, request);
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("result", result);
        resultData.put("message", result == 0?"fail":"Delete successful");
        return ResponseEntity.ok(resultData);
    }
}
