package com.example.demo.server;

import com.example.demo.mapper.H5StatsMapper;
import com.example.demo.model.H5Stats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class H5StatService  {
    @Autowired
    private H5StatsMapper h5StatsMapper;

    public int insertH5Stats(H5Stats h5Stats, List<String> api) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < api.size(); i++) {
            stringBuilder.append(api.get(i));
            if (i != api.size() - 1) {
                stringBuilder.append(",");
            }
        }
        h5Stats.setApi(stringBuilder.toString());
        return h5StatsMapper.insertH5Stats(h5Stats);
    }

    public List<Map<String, Object>> getAllH5Stats() {
        List<H5Stats> h5Stats = h5StatsMapper.getAllH5Stats();
        List<Map<String, Object>> result = new ArrayList<>();
        if (h5Stats == null){
            Map<String, Object> map = new HashMap<>();
            map.put("status", "200");
            map.put("message", "URL对应API为空");
            result.add(map);
            return result;
        }
        h5Stats.forEach(h5Stat -> {
            Map<String, Object> map = new HashMap<>();
            String[] split = h5Stat.getApi().split(",");
            List<String> list = Arrays.stream(split).toList();
            map.put("api", list);
            map.put("url", h5Stat.getUrl());
            map.put("name", h5Stat.getName());
            map.put("description", h5Stat.getDescription());
            result.add(map);
        });
        return result;
    }

    public int deleteH5Stats(String url) {
        return h5StatsMapper.deleteH5Stats(url);
    }
    
    public int updateH5StatsApi(String url, List<String> api,H5Stats h5Stats) {
        String[] apis = api.toArray(new String[0]);
        String apiString = String.join(",", apis);
        return h5StatsMapper.updateH5StatsApi(apiString,h5Stats.getName(),h5Stats.getDescription(), url, h5Stats.getUrl());
    }
     
    public List<String> getH5StatsApi(String url) {
        String apiString = h5StatsMapper.getApiByUrl(url);
        if (apiString == null) {
            return Collections.emptyList();
        }
        String[] apis = apiString.split(",");
        return Arrays.asList(apis);
    }
    
    // 新增方法：处理H5Stats对象的创建逻辑
    public H5Stats createH5StatsFromMap(Map<String, Object> h5StatsMap) {
        H5Stats h5Stats = new H5Stats();
        if (h5StatsMap != null) {
            h5Stats.setUrl((String) h5StatsMap.get("url"));
            h5Stats.setDescription((String) h5StatsMap.get("description"));
            h5Stats.setName((String) h5StatsMap.get("name"));
        }
        return h5Stats;
    }
}