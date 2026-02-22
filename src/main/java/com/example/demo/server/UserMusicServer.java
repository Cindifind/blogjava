package com.example.demo.server;

import com.example.demo.mapper.UserMusicListMapper;
import com.example.demo.model.UserMusicList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserMusicServer {
    private static final Logger log = LoggerFactory.getLogger(UserMusicServer.class);
    @Autowired
    private UserMusicListMapper userMusicListMapper;

    public JSONObject selectMusicList(String email) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("musicList", userMusicListMapper.selectMusicList(email));
        jsonObject.put("code", 200);
        log.info("查询成功");
        return jsonObject;
    }
    public JSONObject insertMusicListAndUpdate(UserMusicList userMusicList) {
        String musicList = userMusicListMapper.selectMusicList(userMusicList.getEmail());
        JSONObject result = new JSONObject();
        if (isJSONArray(userMusicList.getMusicList())) {
            result.put("code",400);
            result.put("message", "添加格式不正确");
            return result;
        }
        if (musicList != null) {
            JSONArray MusicList = new JSONArray(musicList);
            JSONArray upDataMusicList = new JSONArray(userMusicList.getMusicList());
            upDataMusicList.forEach(MusicList::put);
            userMusicList.setMusicList(MusicList.toString());
            int resultUpData = userMusicListMapper.updateMusicList(userMusicList);

            result.put("code", 200);
            if (resultUpData == 1) {
                result.put("message", "添加成功");
            } else {
                result.put("message", "添加失败");
            }
        } else {
            int resultInsert = userMusicListMapper.insertMusicList(userMusicList);
            result.put("code", 200);
            if (resultInsert == 1) {
                result.put("message", "添加成功");
            } else {
                result.put("message", "添加失败");
            }
        }
        log.info(result.getString("message"));
        return result;
    }
    public JSONObject deleteMusicList(UserMusicList userMusicList) {
        String musicList = userMusicListMapper.selectMusicList(userMusicList.getEmail());
        JSONObject result = new JSONObject();

        // 空值检查
        if (musicList == null || isJSONArray(musicList)) {
            result.put("code", 400);
            result.put("message", "歌单数据不存在或格式错误");
            return result;
        }

        JSONArray existingList = new JSONArray(musicList);
        JSONArray deleteList = new JSONArray(userMusicList.getMusicList());

        // 使用 Set 提高查找效率，时间复杂度从 O(n²) 降到 O(n)
        Set<Object> toDelete = new HashSet<>();
        deleteList.forEach(toDelete::add);

        // 过滤保留不需要删除的元素
        JSONArray newList = new JSONArray();
        int deletedCount = 0;

        for (int i = 0; i < existingList.length(); i++) {
            Object item = existingList.get(i);
            if (toDelete.contains(item)) {
                deletedCount++;
            } else {
                newList.put(item);
            }
        }

        userMusicList.setMusicList(newList.toString());
        int updateResult = userMusicListMapper.updateMusicList(userMusicList);

        result.put("code", 200);
        result.put("message", updateResult == 1
                ? "删除成功" + deletedCount + "条数据"
                : "删除失败");

        return result;
    }

    private boolean isJSONArray(String str) {
        try {
            new JSONArray(str);
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
