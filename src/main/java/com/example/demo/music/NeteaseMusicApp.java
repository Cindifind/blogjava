package com.example.demo.music;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeteaseMusicApp {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Safari/537.36 Chrome/91.0.4472.164 NeteaseMusicDesktop/2.10.2.200154";
    private static final String AES_KEY = "e82ckenh8dichen8";
    //    private static final String COOKIE = "MUSIC_U=0003573350841EA30C268796F5673CD7075EEA9736A9A214EBA7BD7A1BB27C9AF3161F02726984F70D478B58B7346DBDD813EEDF514FEBB16B1B29556761796A5D62D10F1AEE851B02EDF570EA9CF0D7E0C420A6A30DE505177CF386A0BD9C352BC4570A6F89D0B6EFEC8F63A2DB9F837904EE85CF1A4D6200A2C7FF3F3B7F273B14A88E34C47C09AC16B61394C3291083905D4C0F5B1FAC72ADF008B2A788D6E727533449AB4059576109C673AD69B681D3E48B572D2820371D13D53362E696AC1666ABD71A8C79C80455A2F496F1599361F2690A7D1077884D18D36773DEEFAF2D64D796DBFAC363EB910A40B93988989E1BF636D0471959A6D8E97947B694A39753901C8C504BBB13067ABB7360A2E24CAE84A03418B7AA4BB03E6D70BC34D99D1BD790A97FB2DBDE2520E4A7631518669BCF8F67EEE1EDFAE294381670FA7DB797393AF6827412EB2AD144198BF3DA9F0E7DCB0092DBD2C797CD96B51ED46B;os=pc;appver=8.9.75;";
    private static final String COOKIE = "MUSIC_U=008F923441A9DAA80858640E2BFE0E7FE4765104FD712C7B9321DAAD8FD9040CDE64365654C6FA0AA9222284A7665D88E80981691D32A188B70059F12F8B9AD6EC0A65EB4CA9B57D456C4DE5CAD3709D40C2BB6E5035A7363C9C7864DE005ADD6CADC7E65A93613A639233D2600E03D8E5B85CBAD0D38436CD444E24D0C553012D6EEE1977E3816FD6B968C09A7F4668CE753665560EAC36EA0B43DB04F25FED6480529D9086FCDBFD84907E5B0C1FE0EE811E68EA29B583C45F77A7B7AF7C11BDC90E35123E5F68C426B53AD8F0D2A6BE95E214E265C0A08A305D1985065E683687C2655005B0B61348725C087A2315DD3FFBAC005ACB8D17C697743B158D2B08A874B228397FC990EE8F8BF3A3B45E91C9D2DA628082D5EB56BCE5D822FB7756ACDF920650F413BBDB9EEB090BD4454B2ED07F3044A1B525A4EE027148143DF6DB1E045F2E46A8E907FA9C7EE55DE38579C535E2D7EE14BBAE3D2B45C2A54E5DB3199ECBB4C72D590E9A24D12124E45B0BE852BD90F1EBAD1415D88D2F84D20BF0306E2799D8F786B64B64AC12DBE9DE9FE8FD2A4C118EF4FC259BF81301FB36;os=pc;appver=8.9.75;";
    private static final Logger log = LoggerFactory.getLogger(NeteaseMusicApp.class);

    // 模拟 PHP 中的 NeteaseMusicAPI 类功能
    public static class NeteaseMusicAPI {
        private final String userAgent = USER_AGENT;
        private final Map<String, String> cookies;

        public NeteaseMusicAPI() {
            this.cookies = new HashMap<>();
            // 设置默认 cookies
            this.cookies.put("os", "pc");
            this.cookies.put("appver", "");
            this.cookies.put("osver", "");
            this.cookies.put("deviceId", "pyncm!");
        }

        /**
         * 十六进制编码
         */
        private String hexDigest(byte[] data) {
            return Hex.encodeHexString(data);
        }

        /**
         * MD5哈希
         */
        private byte[] hashDigest(String text) {
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                return md.digest(text.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException("MD5哈希失败", e);
            }
        }

        /**
         * MD5哈希十六进制
         */
        private String hashHexDigest(String text) {
            byte[] md5Bytes = hashDigest(text);
            return hexDigest(md5Bytes);
        }

        /**
         * PKCS7填充
         */
        private byte[] pkcs7Pad(byte[] data) {
            int pad = 16 - (data.length % 16);
            byte[] padded = new byte[data.length + pad];
            System.arraycopy(data, 0, padded, 0, data.length);
            for (int i = data.length; i < padded.length; i++) {
                padded[i] = (byte) pad;
            }
            return padded;
        }

        /**
         * AES加密 (ECB 模式，无填充)
         */
        private byte[] aesEncrypt(byte[] data) {
            try {
                javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(NeteaseMusicApp.AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/NoPadding");
                cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
                return cipher.doFinal(data);
            } catch (Exception e) {
                throw new RuntimeException("AES加密失败", e);
            }
        }

        /**
         * 构建加密参数
         */
        private String buildEncryptedParams(String urlPath, JSONObject payload) {
            String url2 = urlPath.replace("/eapi/", "/api/");
            String requestId = String.valueOf(new Random().nextInt(10000000) + 20000000); // 20000000-30000000
            JSONObject config = new JSONObject();
            config.put("os", "pc");
            config.put("appver", "");
            config.put("osver", "");
            config.put("deviceId", "pyncm!");
            config.put("requestId", requestId);


            String payloadJson = payload.toString();
            String magicString = "nobody" + url2 + "use" + payloadJson + "md5forencrypt";
            String digest = hashHexDigest(magicString);
            String params = url2 + "-36cd479b6b5-" + payloadJson + "-36cd479b6b5-" + digest;
            byte[] paddedParams = pkcs7Pad(params.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = aesEncrypt(paddedParams);
            return hexDigest(encrypted);
        }

        /**
         * 发送POST请求
         */
        private JSONObject post(String url, String encryptedHex, Map<String, String> additionalCookies) throws UnirestException {
            Map<String, String> allCookies = new HashMap<>(this.cookies);
            if (additionalCookies != null) {
                allCookies.putAll(additionalCookies);
            }

            StringBuilder cookieStrBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : allCookies.entrySet()) {
                cookieStrBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }
            String cookieStr = cookieStrBuilder.toString().trim();

            HttpResponse<String> response = Unirest.post(url)
                    .header("User-Agent", userAgent)
                    .header("Referer", "https://music.163.com/")
                    .header("Cookie", cookieStr)
                    .field("params", encryptedHex)
                    .asString();
            if (response.getStatus() != 200) {
                throw new RuntimeException("HTTP请求失败: " + response.getStatus());
            }
            System.out.println(response.getBody());
            return new JSONObject(response.getBody());
        }

        /**
         * 从静态变量中获取 Cookie
         */
        public Map<String, String> loadCookieFromStatic() {
            Map<String, String> cookies = new HashMap<>();
            String[] cookiePairs = COOKIE.split(";");
            for (String pair : cookiePairs) {
                String[] parts = pair.split("=", 2);
                if (parts.length == 2) {
                    cookies.put(parts[0].trim(), parts[1].trim());
                }
            }
            return cookies;
        }

        /**
         * 解析 163cn.tv 跳转链接，获取真实ID
         */
        public String resolve163cnTvUrl(String url) {
            if (url == null || !url.contains("163cn.tv")) {
                return null;
            }
            try {
                HttpResponse<String> response = Unirest.get(url)
                        .header("User-Agent", userAgent)
                        .asString();

                if (response.getStatus() == 301 || response.getStatus() == 302 || response.getStatus() == 303 ||
                        response.getStatus() == 307 || response.getStatus() == 308) {
                    String redirectUrl = response.getHeaders().getFirst("Location");
                    if (redirectUrl != null) {
                        // 从重定向URL中提取ID
                        Pattern[] patterns = {
                                Pattern.compile("id=(\\d+)"),
                                Pattern.compile("song/(\\d+)"),
                                Pattern.compile("music/(\\d+)"),
                                Pattern.compile("(\\d{6,})")
                        };
                        for (Pattern pattern : patterns) {
                            Matcher matcher = pattern.matcher(redirectUrl);
                            if (matcher.find()) {
                                return matcher.group(1);
                            }
                        }
                    }
                }
            } catch (UnirestException e) {
                // 静默处理错误，返回null
                log.error("解析网易云音乐URL失败", e);
            }
            return null;
        }

        /**
         * 从网易云音乐URL中提取歌曲ID
         */
        public String extractIdFromUrl(String url) {
            if (url == null || !url.contains("music.163.com")) {
                return null;
            }
            try {
                java.net.URI uri = new java.net.URI(url);
                String query = uri.getQuery();
                if (query != null) {
                    String[] pairs = query.split("&");
                    for (String pair : pairs) {
                        int idx = pair.indexOf("=");
                        if (idx > 0 && "id".equals(pair.substring(0, idx))) {
                            String id = pair.substring(idx + 1);
                            if (id.matches("\\d+")) {
                                return id;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error extracting ID from URL: {}", e.getMessage());
            }

            return null;
        }

        public JSONObject getMusicUrl(Object id, String level) {
            Map<String, String> cookies = loadCookieFromStatic();
            String url = "https://interface3.music.163.com/eapi/song/enhance/player/url/v1";
            Map<String, String> allCookies = new HashMap<>(this.cookies);
            if (cookies != null) {
                allCookies.putAll(cookies);
            }
            List<Object> idsList;
            if (id instanceof String) {
                idsList = Collections.singletonList(id);
            } else if (id instanceof Collection) {
                idsList = new ArrayList<>((Collection<?>) id);
            } else {
                throw new IllegalArgumentException("id 必须是 String 或 Collection<String>");
            }
            JSONObject payload = new JSONObject();
            payload.put("ids", idsList);
            payload.put("level", level);
            payload.put("encodeType", "flac");
            header(payload);

            if ("sky".equals(level)) {
                payload.put("immerseType", "c51");
            }
            String urlPath = "/eapi/song/enhance/player/url/v1";
            String encryptedHex = buildEncryptedParams(urlPath, payload);
            return post(url, encryptedHex, allCookies);
        }

        private void header(JSONObject payload) {
            JSONObject header = new JSONObject();
            header.put("os", "pc");
            header.put("appver", "");
            header.put("osver", "");
            header.put("deviceId", "pyncm!");
            header.put("requestId", String.valueOf(new Random().nextInt(10000000) + 20000000)); // 20000000-30000000
            payload.put("header", header);
        }

        private String formatLrcTime(long milliseconds) {
            long totalSeconds = milliseconds / 1000;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            long millis = milliseconds % 1000;

            return String.format("%02d:%02d.%03d", minutes, seconds, millis);
        }

        public String formatDuration(long milliseconds) {
            // 将毫秒转换为秒
            long totalSeconds = milliseconds / 1000;

            // 计算小时、分钟和秒
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            // 格式化为 HH:MM:SS
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        public String convertToStandardLrc(String complexLyric) {
            if (complexLyric == null || complexLyric.isEmpty()) {
                return "";
            }

            StringBuilder standardLrc = new StringBuilder();
            String[] lines = complexLyric.split("\n");

            for (String line : lines) {
                if (line.startsWith("{")) {
                    // 处理创作人员信息行，如 {"t":0,"c":[{"tx":"作词: "},{"tx":"米果",...}]}
                    try {
                        JSONObject jsonLine = new JSONObject(line);
                        if (jsonLine.has("t") && jsonLine.has("c")) {
                            long time = jsonLine.getLong("t");
                            JSONArray contentArray = jsonLine.getJSONArray("c");

                            // 将时间转换为 LRC 时间格式 [mm:ss.SSS]
                            String formattedTime = formatLrcTime(time);

                            // 提取文本内容
                            StringBuilder content = new StringBuilder();
                            for (int i = 0; i < contentArray.length(); i++) {
                                JSONObject contentObj = contentArray.getJSONObject(i);
                                if (contentObj.has("tx")) {
                                    content.append(contentObj.getString("tx"));
                                }
                            }

                            // 添加到标准LRC中
                            standardLrc.append("[").append(formattedTime).append("] ").append(content).append("\n");
                        }
                    } catch (Exception e) {
                        // 如果解析JSON失败，可能是普通LRC行，直接添加
                        standardLrc.append(line).append("\n");
                    }
                } else {
                    // 普通LRC行直接添加
                    standardLrc.append(line).append("\n");
                }
            }

            return standardLrc.toString();
        }

        public JSONObject getSongDetail(String songId) {
            // 从静态变量加载cookie
            Map<String, String> cookies = loadCookieFromStatic();
            // 构建请求URL和参数
            String url = "https://music.163.com/eapi/v3/song/detail";
            JSONObject payload = new JSONObject();
            // 构建c字段，格式为 "歌曲ID_歌曲ID"
            String c = "[" + new JSONObject().put("id", songId).toString() + "]";
            payload.put("c", c);
            header(payload);
            String urlPath = "/eapi/v3/song/detail";
            String encryptedHex = buildEncryptedParams(urlPath, payload);
            // 发送请求获取歌曲详情
            JSONObject rawResult = post(url, encryptedHex, cookies);
            // 构建返回结果
            JSONObject result = new JSONObject();
            if (rawResult.has("songs")) {
                JSONObject song = rawResult.getJSONArray("songs").getJSONObject(0);
                result.put("duration", formatDuration(song.getLong("dt")));
                JSONObject al = song.getJSONObject("al");
                result.put("album", al.getString("name"));
                result.put("albumId", al.getLong("id"));
                result.put("picUrl", al.getString("picUrl"));
                JSONObject ar = song.getJSONArray("ar").getJSONObject(0);
                result.put("artist", ar.getString("name"));
            }
            return result;
        }

        /**
         * 获取歌曲歌词信息
         * 对应PHP中的getLyric.php功能
         *
         * @param songId 歌曲ID，支持直接ID、163cn.tv链接或music.163.com链接
         * @return 包含歌词信息的JSONObject
         */
        public JSONObject getLyric(String songId) {
            // 检查是否为 163cn.tv 域名链接并解析真实ID
            if (songId != null && songId.contains("163cn.tv")) {
                String realId = resolve163cnTvUrl(songId);
                if (realId != null) {
                    songId = realId;
                }
            }

            // 检查是否为网易云音乐URL并提取ID
            if (songId != null && songId.contains("music.163.com")) {
                String extractedId = extractIdFromUrl(songId);
                if (extractedId != null) {
                    songId = extractedId;
                }
            }

            // 从静态变量加载cookie
            Map<String, String> cookies = loadCookieFromStatic();

            // 构建请求URL和参数
            String url = "https://music.163.com/eapi/song/lyric/v1";
            JSONObject payload = new JSONObject();
            payload.put("id", songId);
            payload.put("lv", -1);
            payload.put("kv", -1);
            payload.put("tv", -1);
            payload.put("rv", -1);
            header(payload);

            String urlPath = "/eapi/song/lyric/v1";
            String encryptedHex = buildEncryptedParams(urlPath, payload);

            // 发送请求获取歌词
            JSONObject rawResult = post(url, encryptedHex, cookies);

            // 构建返回结果
            JSONObject result = new JSONObject();
            JSONObject lyricData = new JSONObject();

            // 提取各类歌词
            if (rawResult.has("lrc")) {
                JSONObject lrc = rawResult.getJSONObject("lrc");
                String complexLrc = lrc.optString("lyric", "");
                // 转换为标准LRC格式
                String standardLrc = convertToStandardLrc(complexLrc);
                lyricData.put("lrc", standardLrc);
            } else {
                lyricData.put("lrc", "");
            }

            if (rawResult.has("tlyric")) {
                JSONObject tlyric = rawResult.getJSONObject("tlyric");
                lyricData.put("tlyric", tlyric.optString("lyric", ""));
            } else {
                lyricData.put("tlyric", "");
            }

            if (rawResult.has("romalrc")) {
                JSONObject romalrc = rawResult.getJSONObject("romalrc");
                lyricData.put("romalrc", romalrc.optString("lyric", ""));
            } else {
                lyricData.put("romalrc", "");
            }

            if (rawResult.has("klyric")) {
                JSONObject klyric = rawResult.getJSONObject("klyric");
                lyricData.put("klyric", klyric.optString("lyric", ""));
            } else {
                lyricData.put("klyric", "");
            }

            lyricData.put("time", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
            result.put("data", lyricData);

            return result;
        }


        public List<ModelList> getPlaylistSongInfos(String playlistId) {
            NeteaseMusicAPI api = new NeteaseMusicAPI();
            try {
                // 构建请求URL和参数
                String url = "https://music.163.com/eapi/v6/playlist/detail";
                JSONObject payload = new JSONObject();
                payload.put("id", playlistId);
                payload.put("n", 100000); // 获取全部歌曲
                api.header(payload);

                String urlPath = "/eapi/v6/playlist/detail";
                String encryptedHex = api.buildEncryptedParams(urlPath, payload);

                // 从静态变量加载cookie
                Map<String, String> cookies = api.loadCookieFromStatic();

                // 发送请求
                JSONObject result = api.post(url, encryptedHex, cookies);

                // 解析歌曲ID
                List<ModelList> modelList = new ArrayList<>();
                if (result.has("playlist")) {
                    result = result.getJSONObject("playlist");
                    if (result.has("tracks")) {
                        JSONArray jsonArray = result.getJSONArray("tracks");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            ModelList model = new ModelList();
                            JSONObject track = jsonArray.getJSONObject(i);
                            model.setId(String.valueOf(track.getLong("id")));
                            if (track.has("al")) {
                                JSONObject al = track.getJSONObject("al");
                                model.setPicurl(al.getString("picUrl"));
                                try {
                                    model.setName(al.getString("name"));
                                }catch (Exception e){
                                    model.setName("未知");
                                }
                            }
                            model.setMusicDuration(formatDuration(track.getLong("dt")));
                            try {
                                model.setArtistsname(track.getJSONArray("ar").getJSONObject(0).getString("name"));
                            }catch (Exception e){
                                model.setArtistsname("未知");
                            }
                            modelList.add(model);
                        }
                    }
                }
                return modelList;
            } catch (Exception e) {
                log.error("获取歌单歌曲ID失败", e);
                return new ArrayList<>();
            }
        }
        // ... existing code ...
        public JSONObject searchInfo(String keyword, int limit, int offset){
            try {
                // 构建请求参数
                Map<String, Object> data = new HashMap<>();
                data.put("s", keyword);
                data.put("type", 1); // type 1 表示单曲搜索
                data.put("limit", limit);
                data.put("offset",  offset);


                // 从静态变量加载cookie
                Map<String, String> cookies = loadCookieFromStatic();
                StringBuilder cookieStrBuilder = new StringBuilder();
                for (Map.Entry<String, String> entry : cookies.entrySet()) {
                    cookieStrBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
                }
                String cookieStr = cookieStrBuilder.toString().trim();

                // 发送POST请求
                HttpResponse<String> response = Unirest.post("https://music.163.com/api/cloudsearch/pc")
                        .header("User-Agent", USER_AGENT)
                        .header("Referer", "https://music.163.com/")
                        .header("Cookie", cookieStr)
                        .fields(data)
                        .asString();

                if (response.getStatus() != 200) {
                    throw new RuntimeException("搜索请求失败: " + response.getStatus());
                }

                String responseBody = response.getBody();
                System.out.println("搜索结果: " + responseBody);
                // 解析响应并构建返回格式
                JSONObject result = new JSONObject(responseBody);
                if (result.has("result") && result.getJSONObject("result").has("songs")) {
                    JSONArray songs = result.getJSONObject("result").getJSONArray("songs");
                    JSONArray formattedSongs = new JSONArray();
                    for (int i = 0; i < songs.length(); i++) {
                        ModelList model = new ModelList();
                        JSONObject item = songs.getJSONObject(i);
                        model.setName(item.getString("name"));
                        model.setArtistsname(item.getString("name"));
                        // 提取艺术家名称
                        JSONArray arArray = item.getJSONArray("ar");
                        StringBuilder artists = new StringBuilder();
                        for (int j = 0; j < arArray.length(); j++) {
                            if (j > 0) artists.append("/");
                            JSONObject artist = arArray.getJSONObject(j);
                            artists.append(artist.getString("name"));
                        }
                        model.setArtistsname(artists.toString());
                        // 提取专辑信息
                        JSONObject al = item.getJSONObject("al");
                        model.setPicurl(al.getString("picUrl"));
                        model.setMusicDuration(formatDuration(item.getLong("dt")));
                        model.setId(String.valueOf(item.getLong("id")));
                        formattedSongs.put(model);
                    }

                    JSONObject finalResult = new JSONObject();
                    finalResult.put("lists", formattedSongs);
                    finalResult.put("songCount", result.getJSONObject("result").getLong("songCount"));
                    finalResult.put("code", 200);
                    return finalResult;
                } else {
                    // 没有找到歌曲
                    JSONObject finalResult = new JSONObject();
                    finalResult.put("lists", new JSONArray());
                    finalResult.put("code", 200);
                    return finalResult;
                }
            } catch (Exception e) {
                log.error("搜索音乐失败", e);
                JSONObject errorResult = new JSONObject();
                errorResult.put("list", new JSONArray());
                errorResult.put("code", 500);
                errorResult.put("message", "搜索失败: " + e.getMessage());
                return errorResult;
            }
        }
    }
}
