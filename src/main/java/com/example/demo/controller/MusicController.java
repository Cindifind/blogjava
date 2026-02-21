package com.example.demo.controller;

import com.example.demo.music.ModelList;
import com.example.demo.music.Search;
import com.example.demo.server.ApiUrlServer;
import jakarta.servlet.http.HttpServletRequest;
import org.example.text.client.Client;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MusicController {
    private static final Logger log = LoggerFactory.getLogger(MusicController.class);
    public static String name = "起风了";
    @Autowired
    private ApiUrlServer apiUrlServer;
    @Client(address = "/api/music",name = "music")
    @GetMapping("/music")
    public List<Object> music(HttpServletRequest request){
        //初始时间
        long startTime = System.currentTimeMillis();
        apiUrlServer.UpDataaApiState(request);
        //结束时间
        long endTime = System.currentTimeMillis();
        log.info("请求耗时: {}ms", endTime - startTime);
        JSONObject jsonObject = Search.SearchMp3(name, 10, 0);
        return jsonObject.getJSONArray("lists").toList();
    }

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
}