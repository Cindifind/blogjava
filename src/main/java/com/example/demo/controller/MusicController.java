package com.example.demo.controller;

import com.example.demo.music.Search;
import com.example.demo.server.ApiUrlServer;
import jakarta.servlet.http.HttpServletRequest;
import org.example.text.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class MusicController {
    private static final Logger log = LoggerFactory.getLogger(MusicController.class);
    public static String name = "起风了";
    @Autowired
    private ApiUrlServer apiUrlServer;

    @Client(address = "/api/musicSearch",name = "musicSearch")
    @GetMapping("/musicSearch")
    public void musicSearch(@RequestParam String name,HttpServletRequest request) {
        if (name.equals("Miku")) {
            MusicController.name = null;
        } else {
            MusicController.name = name;
        }
        log.info("搜索关键字: {}", name);
        apiUrlServer.UpDataaApiState(request);
    }

    // 添加获取音乐URL的接口
    @Client(address = "/api/getMusicUrl",name = "getMusicUrl")
    @GetMapping("/getMusicUrl")
    public Map<String, String> getMusicUrl(@RequestParam String id,HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            String url = Search.getMusicUrl(id);
            response.put("url", url);
            response.put("code", "200");
            apiUrlServer.UpDataaApiState(request);
        } catch (Exception e) {
            response.put("code", "500");
            response.put("message", "获取音乐URL失败: " + e.getMessage());
        }
        return response;
    }

    // 添加获取歌词的接口
    @Client(address = "/api/getLyric",name = "getLyric")
    @GetMapping("/getLyric")
    public Map<String, Object> getLyric(@RequestParam String id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String lyric = Search.getLyric(id);
        Map<String, String> lrc = new HashMap<>();
        lrc.put("lyric", lyric);
        response.put("lrc", lrc);
        response.put("code", "200");
        apiUrlServer.UpDataaApiState(request);
        return response;
    }
    @Client(address = "/api/music", name = "music")
    @GetMapping("/music")
    public ResponseEntity<Map<String, Object>> getMusicList(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取歌单成功");
        response.put("list", Search.SearchListInfos("2897487612"));
        return ResponseEntity.ok(response);
    }
}