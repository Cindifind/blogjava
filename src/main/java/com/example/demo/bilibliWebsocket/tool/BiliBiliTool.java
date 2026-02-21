package com.example.demo.bilibliWebsocket.tool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class BiliBiliTool {
    private static final Gson gson = new Gson();

    // WBI 密钥缓存
    private static BiliBiliSecretKey wbiSecretKey;
    private static Date wbiUpdateTime;

    // 固定参数
    private static final int[] ENCRYPT_INDEX = {
            46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
            33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
            61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
            36, 20, 34, 44, 52
    };

    private static final BiliBiliSignKey DEFAULT_SIGN_KEY = new BiliBiliSignKey(
            "1d8b6e7d45233436", "560c52ccd288fed045859ed18bffd973"
    );

    /**
     * 获取 WBI 密钥（带缓存）
     */
    public static BiliBiliSecretKey getWbiSecretKey() throws IOException {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        // 如果缓存为空或已过期，重新获取
        if (wbiSecretKey == null || wbiUpdateTime == null || wbiUpdateTime.before(today.getTime())) {
            // 使用 Unirest 替换 OkHttpClient
            HttpResponse<String> response = Unirest.get("https://api.bilibili.com/x/web-interface/nav")
                    .asString();

            if (response.getStatus() != 200) throw new IOException("请求失败");

            JsonObject json = gson.fromJson(response.getBody(), JsonObject.class);
            JsonObject wbiImg = json.getAsJsonObject("data").getAsJsonObject("wbi_img");

            String imgUrl = wbiImg.get("img_url").getAsString();
            String subUrl = wbiImg.get("sub_url").getAsString();

            // 提取密钥（从 URL 中截取文件名）
            wbiSecretKey = new BiliBiliSecretKey();
            wbiSecretKey.img_key = extractKeyFromUrl(imgUrl);
            wbiSecretKey.sub_key = extractKeyFromUrl(subUrl);

            wbiUpdateTime = new Date(); // 更新缓存时间
        }

        return wbiSecretKey;
    }

    /**
     * 从 URL 提取密钥（去掉路径和后缀）
     */
    private static String extractKeyFromUrl(String url) {
        int lastSlash = url.lastIndexOf('/');
        int lastDot = url.lastIndexOf('.');
        return url.substring(lastSlash + 1, lastDot);
    }

    /**
     * WBI 签名
     */
    public static Map<String, String> wbiSign(Map<String, String> params, BiliBiliSecretKey secret) {
        // 1. 生成 mixin_key
        String key = secret.getImgKey() + secret.getSubKey();
        String mixinKey = Arrays.stream(ENCRYPT_INDEX)
                .mapToObj(i -> String.valueOf(key.charAt(i)))
                .collect(Collectors.joining())
                .substring(0, 32);

        // 2.html. 添加 wts 时间戳
        params.put("wts", String.valueOf(System.currentTimeMillis() / 1000));

        // 3. 排序并拼接参数
        String query = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> encodeParam(entry.getKey()) + "=" + encodeParam(entry.getValue()))
                .collect(Collectors.joining("&"));

        // 4. 计算 w_rid (MD5)
        String w_rid = DigestUtils.md5Hex(query + mixinKey);

        // 5. 返回签名后的参数
        Map<String, String> signedParams = new HashMap<>(params);
        signedParams.put("w_rid", w_rid);
        return signedParams;
    }

    /**
     * URL 编码（兼容 JavaScript 的 encodeURIComponent）
     */
    private static String encodeParam(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%21", "!")
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%2A", "*");
    }

    /**
     * 构建弹幕信息请求 URL
     */
    public static String buildDanmuInfoUrl(long roomId) throws IOException {
        // 1. 获取 WBI 密钥
        BiliBiliSecretKey secret = getWbiSecretKey();

        // 2.html. 准备参数
        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(roomId));
        params.put("type", "0");
        params.put("web_location", "444.8");

        // 3. 签名
        Map<String, String> signedParams = wbiSign(params, secret);

        // 4. 构建 URL
        String baseUrl = "https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo";
        String query = signedParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return baseUrl + "?" + query;
    }
    public static Map<String, String> wbiSign(Map<String, String> params) throws IOException{
        BiliBiliSecretKey secretKey = getWbiSecretKey();
        return wbiSign(params, secretKey);
    }

}
