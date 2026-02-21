package com.example.demo.music;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Search {
    private static final Logger log = LoggerFactory.getLogger(Search.class);
    private static final MusicInfo musicInfo = new MusicInfo();

    public static JSONObject SearchMp3(String name,int limit,int offset) {
        return musicInfo.searchInfo(name, limit, offset);
    }

    //id列表搜索
    public static List<ModelList> SearchListMp3(List<String> list) {
        // 创建一个固定大小的线程池
        List<ModelList> musicList = Collections.synchronizedList(new ArrayList<>());
        long startTime = System.currentTimeMillis();
        for (String id : list) {
            try {
                ModelList modelList = new ModelList();
                modelList.setId(id);
                String detail = musicInfo.musicDetail(modelList.getId());
                System.out.println(detail);
                //每一首歌的响应时间
                JSONObject jsonObject = new JSONObject(detail);
                modelList.setArtistsname(jsonObject.getString("artist"));
                modelList.setName(jsonObject.getString("album"));
                modelList.setMusicDuration(jsonObject.getString("duration"));
                modelList.setPicurl(jsonObject.getString("picUrl"));
                musicList.add(modelList);
            } catch (Exception e) {
                log.error("Error:", e);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("搜索时间：" + (endTime - startTime) + "ms");
        return musicList;
    }

    //歌单列表搜索
    public static List<ModelList> SearchListInfos(String id) {
        return musicInfo.musicPlaylistInfo(id);
    }

    public static String getMusicUrl(String id) {
        return musicInfo.musicUrl(id, "standard");
    }

    public static String getMusicUrl(String id, String level) {
        return musicInfo.musicUrl(id, level);
    }

    public static String getLyric(String id) {
        JSONObject jsonObject = new JSONObject(musicInfo.musicLyric(id));
        jsonObject = jsonObject.getJSONObject("data");
        return jsonObject.getString("lrc");
    }

}