package com.example.demo.controller;

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
    public ResponseEntity<Map<String, Object>> stalk(@RequestBody Map<String, Object> body) {
        Object name = body.get("name");
        Object soft = body.get("soft");
        Object o = map.get(name.toString());
        long time = new Date().getTime();
        JSONObject JSdata;
        if (o == null) {
            JSdata = new JSONObject();
        } else {
            JSdata = new JSONObject(o.toString().replaceAll("=", ":"));
        }

        Map<String, Object> data = JSdata.toMap();
        if (soft == null) {

            if (body.get("phone") == null) {
                return ResponseEntity.ok(Map.of("message", "包体错误"));
            }
            JSONObject phone = new JSONObject(body.get("phone").toString().replaceAll("=", ":"));
            phone.put("time", time);
            data.put("phone", phone.toMap());
        } else {
            Map<String, Object> computer = new HashMap<>();
            computer.put("soft", soft);
            computer.put("time", time);
            data.put("computer", computer);
        }

        map.put(name.toString(), data);
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
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) object;
                boolean hasChanged = false;

                // 检查并移除超过 24 小时的 phone 数据
                Object phoneObj = dataMap.get("phone");
                if (phoneObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> phone = (Map<String, Object>) phoneObj;
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
                    @SuppressWarnings("unchecked")
                    Map<String, Object> computer = (Map<String, Object>) computerObj;
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
