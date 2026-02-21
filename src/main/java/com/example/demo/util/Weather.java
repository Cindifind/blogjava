package com.example.demo.util;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Weather {
    private static final Logger log = LoggerFactory.getLogger(Weather.class);
    public static final String GD_KEY = "6d62dfab890c79b7ff88e88df31acf42";

    public Map<String, Object> getWeather(String ip) {
        HttpResponse<String> response = Unirest.get("https://restapi.amap.com/v3/ip?ip=" + ip + "&key=" + GD_KEY).header("Content-Type", "application/json").asString();
        JSONObject jsonObject = new JSONObject(response.getBody());
        System.out.println(jsonObject);
        try {
            response = Unirest.get("https://restapi.amap.com/v3/weather/weatherInfo?city=" + jsonObject.getString("adcode") + "&key=" + GD_KEY).header("Content-Type", "application/json").asString();
            return new JSONObject(response.getBody()).getJSONArray("lives").getJSONObject(0).toMap();
        } catch (Exception e) {
            log.error("获取天气信息失败");
            return null;
        }
    }

}
