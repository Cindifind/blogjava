package com.example.demo.music;
import com.example.demo.music.NeteaseMusicApp.NeteaseMusicAPI;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MusicInfo {
    private static final Logger log = LoggerFactory.getLogger(MusicInfo.class);
    NeteaseMusicAPI api = new NeteaseMusicAPI();
    public String musicId(String id){
        if (id.contains("163cn.tv")) {
            // 如果是 163cn.tv 域名，先解析获取真实ID
            String realId = api.resolve163cnTvUrl(id);
            if (realId != null) {
                id = realId; // 使用解析出的真实ID
                log.info("解析出的真实歌曲ID: {}", id);
            }
        }
        // 检查是否为网易云音乐URL
        if (id.contains("music.163.com")) {
            // 如果是网易云音乐URL，提取ID
            String extractedId = api.extractIdFromUrl(id);
            if (extractedId != null) {
                id = extractedId; // 使用提取出的ID
                log.info("提取出的歌曲ID: {}", id);
            }
        }
        return id;
    }
    public String musicUrl(String id, String level) {
        id = musicId(id);
        JSONObject rawResult = api.getMusicUrl(id, level);
        return rawResult.getJSONArray("data").getJSONObject(0).getString("url");
    }
    public String musicDetail(String id) {
        id = musicId(id);
        JSONObject rawResult = api.getSongDetail(id);
        return rawResult.toString();
    }
    public String musicLyric(String id) {
        id = musicId(id);
        JSONObject rawResult = api.getLyric(id);
        return rawResult.toString();
    }
    public List<ModelList> musicPlaylistInfo(String id) {
        id = musicId(id);
        return api.getPlaylistSongInfos(id);
    }
    public JSONObject searchInfo(String keyword, int limit, int offset) {
        return api.searchInfo(keyword, limit, offset);
    }
}
