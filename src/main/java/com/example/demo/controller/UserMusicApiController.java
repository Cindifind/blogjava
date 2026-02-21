package com.example.demo.controller;

import com.example.demo.music.ModelList;
import com.example.demo.music.MusicInfo;
import com.example.demo.music.Search;
import com.example.demo.server.ApiUrlServer;
import jakarta.servlet.http.HttpServletRequest;
import org.example.text.client.Client;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserMusicApiController {
    @Autowired
    private ApiUrlServer apiUrlServer;
    // 获取音乐URL
    @Client(address = "/user/musicUrl", name = "musicUrl")
    @GetMapping("/user/musicUrl")
    public ResponseEntity<Map<String, Object>> getMuscUrl(@RequestParam String id, @RequestParam String level ,HttpServletRequest  request){
        Map<String, Object> response = new HashMap<>();
        response.put("url", Search.getMusicUrl(id,level));
        response.put("code", 200);
        response.put("message", "获取音乐URL成功");
        apiUrlServer.UpDataaApiState(request);
        return ResponseEntity.ok(response);
    }
    // 获取音乐信息
    @Client(address = "/user/musicInfo", name = "muscInfo")
    @GetMapping("/user/musicInfo")
    public ResponseEntity<Map<String, Object>> getMuscInfo(@RequestParam String id, HttpServletRequest  request){
        Map<String, Object> response = new HashMap<>();
        MusicInfo musicInfo = new MusicInfo();
        JSONObject jsonObject = new JSONObject(musicInfo.musicDetail(id));
        ModelList modelList = new ModelList();
        modelList.setArtistsname(jsonObject.getString("artist"));
        modelList.setName(jsonObject.getString("album"));
        modelList.setMusicDuration(jsonObject.getString("duration"));
        modelList.setPicurl(jsonObject.getString("picUrl"));
        modelList.setId(id);
        response.put("code", 200);
        response.put("message", "获取音乐信息成功");
        response.put("info", modelList);
        apiUrlServer.UpDataaApiState(request);
        return ResponseEntity.ok(response);
    }
    // 获取音乐歌词
    @Client(address = "/user/musicLyric", name = "musicLyric")
    @GetMapping("/user/musicLyric")
    public ResponseEntity<Map<String, Object>> getMusicLyric(@RequestParam String id, HttpServletRequest  request){
        Map<String, Object> response = new HashMap<>();
        String lyric = Search.getLyric(id);
        response.put("code", 200);
        response.put("message", "获取音乐歌词成功");
        response.put("lyric", lyric);
        apiUrlServer.UpDataaApiState(request);
        return ResponseEntity.ok(response);
    }
    // 获取歌单
    @Client(address = "/user/musicList", name = "musicList")
    @GetMapping("/user/musicList")
    public ResponseEntity<Map<String, Object>> getMusicList(@RequestParam String id, HttpServletRequest  request){
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取歌单成功");
        response.put("list", Search.SearchListInfos(id));
        apiUrlServer.UpDataaApiState(request);
        return ResponseEntity.ok(response);
    }
    // 搜索歌曲
    @Client(address = "/user/musicSearch", name = "userMusicSearch")
    @GetMapping("/user/musicSearch")
    public ResponseEntity<Map<String, Object>> getMusicSearch(@RequestParam String name,@RequestParam int l,@RequestParam int t, HttpServletRequest  request){
        Map<String, Object> response = Search.SearchMp3(name, l, t).toMap();
        response.put("page",t);
        response.put("size",l);
        apiUrlServer.UpDataaApiState(request);
        return ResponseEntity.ok(response);
    }
    // 获取歌单列表信息
    @Client(address = "/user/musicListInfo", name = "musicListInfo")
    @PostMapping("/user/musicListInfo")
    public ResponseEntity<Map<String, Object>> getMusicListInfo(@RequestBody List<String> id, HttpServletRequest  request){
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取歌单信息成功");
        response.put("list", Search.SearchListMp3(id));
        apiUrlServer.UpDataaApiState(request);
        return ResponseEntity.ok(response);
    }

}
