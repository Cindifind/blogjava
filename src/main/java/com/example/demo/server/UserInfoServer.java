package com.example.demo.server;

import com.example.demo.auth.mapper.UserInfoMapper;
import com.example.demo.auth.model.UserInfo;
import com.example.demo.auth.util.Argon2Util;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserInfoServer {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserInfoMapper userInfoMapper;
    private static final Logger log = LoggerFactory.getLogger(UserInfoServer.class);

    public Map<String, Object> login(String emailHash, String password) {
        String email = redisTemplate.opsForValue().get(emailHash);
        UserInfo userInfo = userInfoMapper.getUserInfoByEmail(email);
        if (email == null || userInfo == null || !equalsChar(password, userInfo.getPassword())) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", "用户不存在或账号密码不正确");
            return response;
        }

        // 检查Redis中是否已有可用的token
        // 使用特定的键格式: user:refresh_token:{email} 和 user:access_token:{email}
        String refreshTokenKey = "user:refresh_token:" + email;
        String accessTokenKey = "user:access_token:" + email;

        String existingRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);
        String existingAccessToken = redisTemplate.opsForValue().get(accessTokenKey);

        String refreshToken;
        String accessToken;

        if (existingRefreshToken != null && existingAccessToken != null) {
            // 情况1: 两个token都存在且有效，直接使用
            refreshToken = existingRefreshToken;
            accessToken = existingAccessToken;
        } else if (existingRefreshToken != null) {
            // 情况2: 只有refreshToken存在，使用refresh方法获取新的accessToken
            Map<String, Object> refreshResponse = refresh(existingRefreshToken);
            if ((int) refreshResponse.get("code") == 200) {
                refreshToken = (String) refreshResponse.get("refreshToken");
                accessToken = (String) refreshResponse.get("accessToken");
                // 更新Redis中的token
                storeTokensInRedis(email, refreshToken, accessToken);
            } else {
                // refresh失败，重新生成两个token
                refreshToken = generateRefreshToken(48);
                accessToken = generateRefreshToken(36);
                storeTokensInRedis(email, refreshToken, accessToken);
            }
        } else {
            // 情况3: 都没有，重新生成两个token
            refreshToken = generateRefreshToken(48);
            accessToken = generateRefreshToken(36);
            storeTokensInRedis(email, refreshToken, accessToken);
        }

        Map<String, Object> response = new HashMap<>();
        userInfo.setPassword(null);
        response.put("code", 200);
        response.put("message", "登录成功");
        response.put("data", userInfo);
        try {
            HttpResponse<String> name = Unirest.get("https://users.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins=" + userInfo.getEmail().replaceAll("@qq.com", ""))
                    .header("Accept", "application/vnd.github.v3+json")
                    .asString();
            JSONObject QQname = new JSONObject(name.getBody().replaceAll(".*\\((.*)\\)", "$1"));
            JSONArray QQInfoArray = QQname.getJSONArray(userInfo.getEmail().replaceAll("@qq.com", ""));
            userInfo.setImgUrl(QQInfoArray.getString(0));
            userInfo.setName(QQInfoArray.getString(6));
        } catch (Exception e) {
            log.info("非qq邮箱登录");
        }

        userInfo.setAccessToken(accessToken);
        userInfo.setRefreshToken(refreshToken);
        return response;
    }

    private void storeTokensInRedis(String email, String refreshToken, String accessToken) {
        // 使用特定的键格式存储token
        String refreshTokenKey = "user:refresh_token:" + email;
        String accessTokenKey = "user:access_token:" + email;

        // token到email的反向映射键
        String refreshTokenMappingKey = "token:refresh:" + refreshToken;
        String accessTokenMappingKey = "token:access:" + accessToken;

        // 使用 Lua 脚本原子性地设置所有键值对
        String luaScript =
                """
                        redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
                        redis.call('SET', KEYS[2], ARGV[3], 'EX', ARGV[4])
                        redis.call('SET', KEYS[3], ARGV[5], 'EX', ARGV[6])
                        redis.call('SET', KEYS[4], ARGV[7], 'EX', ARGV[8])
                        return 1""";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        redisTemplate.execute(redisScript,
                Arrays.asList(refreshTokenKey, accessTokenKey, refreshTokenMappingKey, accessTokenMappingKey),
                refreshToken, String.valueOf(TimeUnit.DAYS.toSeconds(14)),
                accessToken, String.valueOf(TimeUnit.HOURS.toSeconds(1)),
                email, String.valueOf(TimeUnit.DAYS.toSeconds(14)),
                email, String.valueOf(TimeUnit.HOURS.toSeconds(1)));
    }

    public Map<String, Object> getSalt(String email, String hash) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取盐成功");
        String isTrueEmail = Argon2Util.argon2Hash(email, Argon2Util.SALT);
        if (!isTrueEmail.equals(hash)) {
            response.put("data", Argon2Util.generateRandomSalt());
            return response;
        }
        String salt = userInfoMapper.getSalt(email);
        if (salt == null) {
            salt = Argon2Util.generateRandomSalt();
        } else {
            // 使用 setIfAbsent 实现原子操作（仅当 key 不存在时设置）
            redisTemplate.opsForValue().setIfAbsent(hash, email, 5, TimeUnit.SECONDS);
        }
        response.put("data", salt);
        return response;
    }

    public Map<String, Object> refresh(String refreshToken) {
        Map<String, Object> response = new HashMap<>();

        // 先通过refreshToken查询email
        String refreshTokenMappingKey = "token:refresh:" + refreshToken;
        String email = redisTemplate.opsForValue().get(refreshTokenMappingKey);

        if (email == null) {
            response.put("code", 401);
            response.put("message", "刷新令牌无效");
            return response;
        }

        // 使用 Lua 脚本原子性地检查 token、获取过期时间、删除和设置新 token
        String luaScript =
                """
                        local emailVal = redis.call('GET', KEYS[1])
                        if not emailVal then
                            return {0, '', ''}
                        end
                        local ttl = redis.call('TTL', KEYS[1])
                        local DAY7 = 7 * 24 * 60 * 60
                        local refreshTokenNew = ''
                        if ttl < DAY7 then
                            refreshTokenNew = ARGV[1]
                            redis.call('SET', KEYS[2], emailVal, 'EX', ARGV[2])
                            redis.call('DEL', KEYS[1])
                            -- 删除旧的token映射
                            redis.call('DEL', KEYS[4])
                        end
                        local accessToken = ARGV[3]
                        local newRefreshToken = refreshTokenNew ~= '' and refreshTokenNew or KEYS[1]
                        redis.call('SET', KEYS[3], accessToken, 'EX', ARGV[4])
                        -- 设置新的token映射
                        if refreshTokenNew ~= '' then
                            redis.call('SET', KEYS[5], emailVal, 'EX', ARGV[2])
                        end
                        redis.call('SET', KEYS[6], emailVal, 'EX', ARGV[4])
                        return {1, emailVal, refreshTokenNew, accessToken}""";

        String refreshTokenNew = generateRefreshToken(48);
        String accessToken = generateRefreshToken(36);

        String oldRefreshTokenMappingKey = "token:refresh:" + refreshToken;
        String newRefreshTokenMappingKey = "token:refresh:" + refreshTokenNew;
        String accessTokenMappingKey = "token:access:" + accessToken;

        @SuppressWarnings("rawtypes")
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(luaScript, List.class);
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) redisTemplate.execute(redisScript,
                Arrays.asList(refreshToken, refreshTokenNew, accessToken, oldRefreshTokenMappingKey, newRefreshTokenMappingKey, accessTokenMappingKey),
                refreshTokenNew,
                String.valueOf(TimeUnit.DAYS.toSeconds(14)),
                accessToken,
                String.valueOf(TimeUnit.HOURS.toSeconds(1)));

        if (result == null || result.isEmpty() || ((Number) result.get(0)).intValue() == 0) {
            response.put("code", 401);
            response.put("message", "刷新令牌无效");
            return response;
        }

        String returnedRefreshToken = ((Number) result.get(0)).intValue() == 1 && !result.get(2).toString().isEmpty()
                ? result.get(2).toString()
                : refreshToken;

        response.put("code", 200);
        response.put("message", "刷新成功");
        response.put("refreshToken", returnedRefreshToken);
        response.put("accessToken", result.get(3).toString());
        return response;
    }

    // 通过accessToken查询email
    public String getEmailByAccessToken(String accessToken) {
        String accessTokenMappingKey = "token:access:" + accessToken;
        return redisTemplate.opsForValue().get(accessTokenMappingKey);
    }

    // 通过refreshToken查询email
    public String getEmailByRefreshToken(String refreshToken) {
        String refreshTokenMappingKey = "token:refresh:" + refreshToken;
        return redisTemplate.opsForValue().get(refreshTokenMappingKey);
    }

    private String generateRefreshToken(int length) {
        byte[] token = new byte[length];
        new SecureRandom().nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private boolean equalsChar(String str1, String str2) {
        boolean result = true;
        char[] charArray1 = str1.toCharArray();
        char[] charArray2 = str2.toCharArray();
        if (charArray1.length != charArray2.length) {
            return false;
        }
        for (int i = 0; i < charArray1.length; i++) {
            if (charArray1[i] != charArray2[i]) {
                result = false;
            }
        }
        return result;
    }
}