package com.example.demo.bilibliWebsocket.map;


import java.util.HashMap;
import java.util.Map;


public class Datacenter {
    public static Map<String, String> getCookieMap(String cookie){
        Map<String, String> cookieMap = new HashMap<>();
        String[] pairs = cookie.split("; ");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx > 0 && idx < pair.length() - 1) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                cookieMap.put(key, value);
            }
        }
        return cookieMap;
    }
}
