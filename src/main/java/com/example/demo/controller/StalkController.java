package com.example.demo.controller;

import com.example.demo.util.IPConfig;
import com.example.demo.util.WeatherUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.example.text.client.Client;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class StalkController {
    private static final Map<String, Object> map = new HashMap<>();

    @Client(address = "/stalk", name = "stalk")
    @PostMapping("/stalk")
    public ResponseEntity<Map<String, Object>> stalk(@RequestBody Map<String, Object> body, HttpServletRequest request) {

        // 1. IP & 天气定位
        String ip = new IPConfig().getClientIP(request);
        WeatherUtil weatherUtil = new WeatherUtil();
        JSONObject locateByIp = weatherUtil.getLocateByIp(ip);
        JSONObject result = locateByIp.getJSONObject("result").getJSONObject("ad_info");

        // 2. 基础参数（防 NPE）
        Object nameObj = body.get("name");
        if (nameObj == null) {
            return ResponseEntity.ok(Map.of("message", "name 不能为空"));
        }
        String name = nameObj.toString();

        Object soft = body.get("soft");
        long time = System.currentTimeMillis();

        // 3. 从缓存中取历史数据（✅ 不再转字符串）
        Map<String, Object> data;
        Object cached = map.get(name);
        if (cached instanceof Map) {
            data = new HashMap<>((Map<String, Object>) cached);
        } else {
            data = new HashMap<>();
        }

        // 4. 写入定位信息
        data.put("location", result.toMap());

        // 5. phone / computer 分支
        if (soft == null) {
            Object phoneObj = body.get("phone");
            if (phoneObj == null) {
                return ResponseEntity.ok(Map.of("message", "包体错误"));
            }

            // ✅ phone 本身就是 Map，直接使用
            Map<String, Object> phoneMap;
            if (phoneObj instanceof Map) {
                phoneMap = new HashMap<>((Map<String, Object>) phoneObj);
            } else {
                phoneMap = new HashMap<>();
            }

            phoneMap.put("time", time);
            data.put("phone", phoneMap);
        } else {
            Map<String, Object> computer = new HashMap<>();
            computer.put("soft", soft);
            computer.put("time", time);
            data.put("computer", computer);
        }

        // 6. 写回缓存（✅ 存 Map，不存 JSONObject）
        map.put(name, data);

        return ResponseEntity.ok(Map.of("message", "已记录"));
    }

    @Client(address = "/stalkLook", name = "stalkLook")
    @GetMapping("/stalkLook")
    public ResponseEntity<Object> stalkLook(@RequestParam String name) {
        long currentTime = new Date().getTime();

        // 遍历所有条目，清理超过 24 小时的数据，防止内存泄露
        Set<String> strings = map.keySet();
        for (String key : strings) {
            Object object = map.get(key);
            if (object instanceof Map) {
                @SuppressWarnings("unchecked") Map<String, Object> dataMap = (Map<String, Object>) object;
                boolean hasChanged = false;

                // 检查并移除超过 24 小时的 phone 数据
                Object phoneObj = dataMap.get("phone");
                if (phoneObj instanceof Map) {
                    @SuppressWarnings("unchecked") Map<String, Object> phone = (Map<String, Object>) phoneObj;
                    Object timeData = phone.get("time");
                    if (timeData != null) {
                        long phoneTime = Long.parseLong(timeData.toString());
                        if (currentTime - phoneTime > 1000 * 60 * 60 * 24) {
                            dataMap.remove("phone");
                            hasChanged = true;
                        }
                    }
                }

                // 检查并移除超过 24 小时的 computer 数据
                Object computerObj = dataMap.get("computer");
                if (computerObj instanceof Map) {
                    @SuppressWarnings("unchecked") Map<String, Object> computer = (Map<String, Object>) computerObj;
                    Object timeData = computer.get("time");
                    if (timeData != null) {
                        long computerTime = Long.parseLong(timeData.toString());
                        if (currentTime - computerTime > 1000 * 60 * 60 * 24) {
                            dataMap.remove("computer");
                            hasChanged = true;
                        }
                    }
                }

                // 如果 phone 和 computer 都被移除了，则移除整个 name 的记录
                if (dataMap.isEmpty()) {
                    map.remove(key);
                } else if (hasChanged) {
                    // 只有部分数据被移除时，更新该记录
                    map.put(key, dataMap);
                }
            }
        }

        if (!map.containsKey(name)) {
            return ResponseEntity.ok(Map.of("message", "该小伙伴没有被视监"));
        }
        return ResponseEntity.ok(map.get(name));
    }


}
