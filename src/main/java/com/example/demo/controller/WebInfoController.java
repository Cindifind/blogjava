package com.example.demo.controller;

import com.example.demo.mapper.WebInfoMapper;
import com.example.demo.server.ApiUrlServer;
import com.example.demo.util.IPConfig;
import com.example.demo.util.Weather;
import jakarta.servlet.http.HttpServletRequest;
import org.example.text.client.Client;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WebInfoController {
    private static final Logger log = LoggerFactory.getLogger(WebInfoController.class);
    @Autowired
    private WebInfoMapper webInfoMapper;
    @Autowired
    private ApiUrlServer apiUrlServer;
    @Client(address = "/api/getWeather",name = "getWeather")
    @RequestMapping("/api/getWeather")
    public Map<String, Object> getWeather(@RequestParam(value = "ipApi", required = false) String ipApi, HttpServletRequest request) {
        String ip;
        if (ipApi == null || ipApi.isEmpty()) {
            // 没有传入ipApi参数，使用客户端IP
            ip = new IPConfig().getClientIP(request);
            log.info("获取天气信息，ip: {}", ip);
        } else {
            // 使用传入的ipApi参数
            if (ipApi.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(ipApi);
                try {
                    ip = jsonObject.getString("ipApi");
                } catch (Exception e) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("message", "字段不正确");
                    result.put("status", "500");
                    return result;
                }
            } else {
                ip = ipApi;
            }
            log.info("获取天气信息，传入ip: {}", ip);
        }
        return getStringObjectMap(ip, request);
    }
    @Client(address = "/info/getPageViews",name = "getPageViews")
    @RequestMapping("/info/getPageViews")
    public Map<String, Object> getPageViews() {
        long pageViews = webInfoMapper.getPageViews();

        Map<String, Object> result = new HashMap<>();
        result.put("status", "200");
        result.put("message", "获取页面访问量成功");
        result.put("view", pageViews);
        return result;
    }

    private Map<String, Object> getStringObjectMap(String ipApi,HttpServletRequest request) {
        Map<String, Object> weather = new Weather().getWeather(ipApi);
        if (weather == null) {
            weather = new HashMap<>();
            weather.put("status", "201");
            weather.put("message", "当前网络为ipv6，仅可使用ipv4调用此接口" + ipApi);
            apiUrlServer.UpDataaApiState(request);
            return weather;
        }
        weather.put("ip", ipApi);
        weather.put("status", "success");
        weather.put("message", "获取天气信息成功");
        apiUrlServer.UpDataaApiState(request);
        return weather;
    }
}
