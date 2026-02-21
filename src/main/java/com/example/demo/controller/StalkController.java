package com.example.demo.controller;

import org.example.text.client.Client;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class StalkController {
    private static final Map<String,Object> map = new HashMap<>();
    @Client(address = "/stalk", name = "stalk")
    @PostMapping("/stalk")
    public ResponseEntity<Map<String, Object>> stalk(@RequestBody Map<String, Object> body) {
        Object name = body.get("name");
        Object soft = body.get("soft");
        Map<String,Object> data = new HashMap<>();
        data.put("soft",soft);
        data.put("time",new Date().getTime());
        map.put(name.toString(),data);
        return ResponseEntity.ok(Map.of("message", "已记录"));
    }
    @Client(address = "/stalkLook", name = "stalkLook")
    @GetMapping("/stalkLook")
    public ResponseEntity<Object> stalkLook(@RequestParam String name) {
        Set<String> strings = map.keySet();
        for (String string : strings) {
            Object object = map.get(string);
            if (object instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String,Object> time = (Map<String, Object>) object;
                Object data = time.get("time");
                long end = Long.parseLong(data.toString());
                if (new Date().getTime() - end > 1000 * 60 * 60 * 24){
                    map.remove(string);
                }
            }
        }
        if (!map.containsKey(name)) {
            return ResponseEntity.ok(Map.of("message", "该小伙伴没有被视监"));
        }
        return ResponseEntity.ok(map.get(name));
    }
}
