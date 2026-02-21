package com.example.demo.bilibliWebsocket;

import org.json.JSONArray;
import org.json.JSONObject;

public class CleanMessage {
    public static String cleanMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            JSONArray array = jsonObject.getJSONArray("info");
            return array.get(1).toString();
        }catch (Exception e){
            return null;
        }
    }
}
