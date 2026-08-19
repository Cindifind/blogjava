package com.example.demo.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * QQ音乐分享数据生成器（固定appid=102820365）
 * 修正版：使用正确的 ZZC 签名算法和 payload 构造方式
 */
public class MusicShareGenerator {

    // ==================== 固定配置 ====================
    private static final String APP = "com.tencent.music.lua";
    private static final String BIZSRC = "qqconnect.sdkshare_music";
    private static final String VIEW = "music";
    private static final String VER = "0.0.0.1";
    private static final int APP_TYPE = 1;
    private static final int APPID = 102820365;
    private static final long UIN = 3960857991L;
    private static final String TAG = "CZAPI";
    private static final String TAG_ICON = "https://p.qpic.cn/qqconnect/0/app_102820365_1766662023/100?max-age=2592000&t=0";

    private static final String DEFAULT_PREVIEW = "https://qq.ugcimg.cn/v1/mtla5as167qh0qiq9lqv6q3tc272g9t1nei8sh67hq9mpi6igqfa1tmv6tkj79djvtprp39dm4mi4rf9jvt5fhplcaggpvnqcqnot88sf8kfrm2kvbdmqubrp80eo0q1/plclip1nnhrldvpvtiio1hjqh0";

    // ==================== ZZC签名常量（修正） ====================
    // 修正：去掉了索引 40（因为 SHA-1 长度为 40，索引范围 0~39）
    private static final int[] PART_1_INDEXES = {23, 14, 6, 36, 16, 7, 19};
    private static final int[] PART_2_INDEXES = {16, 1, 32, 12, 19, 27, 8, 5};
    private static final int[] SCRAMBLE_VALUES = {
            89, 39, 179, 150, 218, 82, 58, 252, 177,
            52, 186, 123, 120, 64, 242, 133, 143, 161, 121, 179
    };

    // ==================== 主要转换方法 ====================

    /**
     * 将简化请求体转换为完整的QQ音乐分享格式
     * @param simpleData 简化数据 {url, audio, title, desc, image}
     * @return 完整的QQ音乐分享数据
     */
    public static Map<String, Object> convertToShareData(Map<String, Object> simpleData,long time) throws Exception {
        String url = (String) simpleData.get("url");
        String audio = (String) simpleData.get("audio");
        String title = (String) simpleData.get("title");
        String desc = (String) simpleData.get("desc");
        String image = (String) simpleData.get("image");

//        long ctime = System.currentTimeMillis() / 1000;
        long ctime = time;

        // 使用 TreeMap 保证字段顺序一致（虽然签名不再依赖 JSON 顺序，但为了可读性保留）
        Map<String, Object> result = new TreeMap<>();
        result.put("app", APP);
        result.put("bizsrc", BIZSRC);
        result.put("ver", VER);
        result.put("view", VIEW);
        result.put("prompt", "[分享]" + title);

        Map<String, Object> config = new TreeMap<>();
        config.put("ctime", ctime);
        config.put("forward", 1);
        config.put("type", "normal");
        result.put("config", config);

        Map<String, Object> extra = new TreeMap<>();
        extra.put("app_type", APP_TYPE);
        extra.put("appid", APPID);
        extra.put("uin", UIN);
        result.put("extra", extra);

        Map<String, Object> music = new TreeMap<>();
        music.put("app_type", APP_TYPE);
        music.put("appid", APPID);
        music.put("ctime", ctime);
        music.put("desc", desc);
        music.put("jumpUrl", url);
        music.put("musicUrl", audio);
        music.put("preview", image);
        music.put("tag", TAG);
        music.put("tagIcon", TAG_ICON);
        music.put("title", title);
        music.put("uin", UIN);

        Map<String, Object> meta = new TreeMap<>();
        meta.put("music", music);
        result.put("meta", meta);

        // ========== 修正点：使用参数拼接串作为签名 payload ==========
        String payload = buildSignPayload(result);
        String token = generateToken(payload);
        config.put("token", token);

        return result;
    }

    // ==================== 新增：构造签名 payload ====================

    /**
     * 构造用于签名的参数字符串（按字典序排列的 key=value&）
     * 仅使用 meta.music 中的字段参与签名，ctime 为当前时间的 10 位时间戳
     */
    private static String buildSignPayload(Map<String, Object> shareData) {
        TreeMap<String, String> params = new TreeMap<>();

        // 仅提取 meta.music 中参与签名的字段
        Map<String, Object> meta = (Map<String, Object>) shareData.get("meta");
        Map<String, Object> music = (Map<String, Object>) meta.get("music");
        params.put("meta.music.app_type", String.valueOf(music.get("app_type")));
        params.put("meta.music.appid", String.valueOf(music.get("appid")));
        params.put("meta.music.ctime", String.valueOf(music.get("ctime")));
        params.put("meta.music.desc", getString(music, "desc"));
        params.put("meta.music.jumpUrl", getString(music, "jumpUrl"));
        params.put("meta.music.musicUrl", getString(music, "musicUrl"));
        params.put("meta.music.preview", getString(music, "preview"));
        params.put("meta.music.tag", getString(music, "tag"));
        params.put("meta.music.tagIcon", getString(music, "tagIcon"));
        params.put("meta.music.title", getString(music, "title"));
        params.put("meta.music.uin", String.valueOf(music.get("uin")));

        // 拼接成 key=value&key=value 格式
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    // ==================== Token生成 ====================

    private static String generateToken(String payload) throws Exception {
        String zzcSign = zzcSign(payload);
        return md5(zzcSign);
    }

    /**
     * ZZC签名算法（修正）
     */
    private static String zzcSign(String payload) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = md.digest(payload.getBytes(StandardCharsets.UTF_8));
        String hash = bytesToHex(hashBytes);

        // Part1：直接取索引（所有索引均有效）
        StringBuilder part1 = new StringBuilder();
        for (int idx : PART_1_INDEXES) {
            part1.append(hash.charAt(idx));
        }

        StringBuilder part2 = new StringBuilder();
        for (int idx : PART_2_INDEXES) {
            part2.append(hash.charAt(idx));
        }

        byte[] part3 = new byte[20];
        for (int i = 0; i < 20; i++) {
            int hexValue = Integer.parseInt(hash.substring(i * 2, i * 2 + 2), 16);
            part3[i] = (byte) (SCRAMBLE_VALUES[i] ^ hexValue);
        }

        String b64Part = Base64.getEncoder().encodeToString(part3)
                .replaceAll("[\\\\/+=]", "");

        return ("zzc" + part1.toString() + b64Part + part2.toString()).toLowerCase();
    }

    // ==================== 工具方法 ====================

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    /**
     * Map转JSON字符串（保留用于调试，但签名不再使用）
     */
    public static String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (json.length() > 1) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Map) {
                json.append(mapToJson((Map<String, Object>) value));
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }
        json.append("}");
        return json.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static void main(String[] args) throws Exception {
        String s = "{\n" +
                "  \"type\": \"cz\",\n" +
                "  \"url\": \"https://api.czcn.xyz/\",\n" +
                "  \"audio\": \"https://api.czcn.xyz/\",\n" +
                "  \"title\": \"CZ - API\",\n" +
                "  \"desc\": \"CZ科技\",\n" +
                "  \"image\": \"https://api.czcn.xyz/Public/Uploads/Images/0a4e78d0e419e8917450bff93e2e2db4.jpg\"\n" +
                "}";
        System.out.println(new JSONObject(convertToShareData(new JSONObject(s).toMap(),1786888426)));
    }
}