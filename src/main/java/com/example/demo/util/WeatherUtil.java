package com.example.demo.util;

import com.example.demo.model.Weather;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WeatherUtil {
    private static final Logger log = LoggerFactory.getLogger(WeatherUtil.class);
    public static final String GD_KEY = "6d62dfab890c79b7ff88e88df31acf42";
    public static final String TX_KEY = "SMFBZ-H6XW4-K7FUF-KGBAG-S6PD5-Q3FSV";

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
    public  Weather getWeatherByTX(String ipApi) {
        Weather weather = new Weather();
        HttpResponse<String> responseIp = Unirest.get("https://apis.map.qq.com/ws/location/v1/ip?ip="+ipApi+"&&key="+TX_KEY)
                .header("Content-Type", "application/json")
                .asString();
        JSONObject resultByIp = new JSONObject(responseIp.getBody());
        resultByIp = resultByIp.getJSONObject("result");
        weather.setIp(resultByIp.getString("ip"));
        resultByIp = resultByIp.getJSONObject("ad_info");
        weather.setCity(resultByIp.getString("city"));
        weather.setAdcode(resultByIp.getInt("adcode"));
        weather.setProvince(resultByIp.getString("province"));
        HttpResponse<String> responseWeather = Unirest.get("https://apis.map.qq.com/ws/weather/v1/?adcode=310000&&key="+TX_KEY)
                .header("Content-Type", "application/json")
                .asString();
        JSONObject resultByWeather = new JSONObject(responseWeather.getBody());
        resultByWeather = resultByWeather.getJSONObject("result").getJSONArray("realtime").getJSONObject(0);
        weather.setReporttime(resultByWeather.getString("update_time"));
        resultByWeather = resultByWeather.getJSONObject("infos");
        weather.setWeather(resultByWeather.getString("weather"));
        weather.setHumidity(resultByWeather.getInt("humidity"));
        weather.setWindpower(resultByWeather.getString("wind_power"));
        weather.setWinddirection(resultByWeather.getString("wind_direction"));
        weather.setTemperature(resultByWeather.getInt("temperature"));
        weather.setStatus("success");
        return weather;
    }

}
