package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/util")
public class UtilController {

    private static final Logger logger = LoggerFactory.getLogger(UtilController.class);

    // Nginx配置文件路径
    private static final String NGINX_CONF_PATH = "/www/server/nginx/conf/nginx.conf";

    // 存储客户端标识与其IP地址的映射
    private final Map<String, String> clientIpMap = new ConcurrentHashMap<>();

    @PostMapping("/ipv6")
    public String ipv6(@RequestBody Map<String, Object> requestBody) {
        try {
            // 从请求体中获取客户端IP地址
            String currentIp = getClientIpFromRequest(requestBody);
            // 获取客户端标识
            String clientId = getClientId(requestBody);
            // 记录客户端信息
            logClientInfo(clientId, currentIp, requestBody);
            // 更新客户端IP映射
            String oldIp = clientIpMap.put(clientId, currentIp);
            // 处理客户端IP更新
            handleClientIpUpdate(clientId, currentIp, oldIp);
            // 更新Nginx配置
            updateNginxServerBlock(currentIp);
            // 重新加载Nginx配置
            reloadNginxConfig();
            //
            return "200";
        } catch (Exception e) {
            logger.error("Error processing ipv6 request: {}", e.getMessage(), e);
            return "500 - Error: " + e.getMessage();
        }
    }

    /**
     * 记录客户端信息
     */
    private void logClientInfo(String clientId, String currentIp, Map<String, Object> requestBody) {
        logger.info("Client ID: {}", clientId);
        logger.info("Current IP: {}", currentIp);

        // 记录请求体中的其他数据
        if (logger.isDebugEnabled()) {
            StringBuilder debugInfo = new StringBuilder("Request body details:");
            for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                debugInfo.append("\n  ").append(entry.getKey()).append(": ").append(entry.getValue());
            }
            logger.debug(debugInfo.toString());
        }
    }

    /**
     * 处理客户端IP更新日志
     */
    private void handleClientIpUpdate(String clientId, String newIp, String oldIp) {
        if (oldIp != null && !oldIp.equals(newIp)) {
            logger.info("Updated client {} IP from {} to {}", clientId, oldIp, newIp);
        } else if (oldIp == null) {
            logger.info("New client {} registered with IP {}", clientId, newIp);
        }
    }

    /**
     * 从请求体中获取客户端IP地址
     */
    private String getClientIpFromRequest(Map<String, Object> requestBody) {
        // 假设IP地址在请求体的"ip"字段中
        if (requestBody.containsKey("ip")) {
            return requestBody.get("ip").toString();
        }
        throw new IllegalArgumentException("Missing IP address in request body");
    }

    /**
     * 获取客户端标识
     */
    private String getClientId(Map<String, Object> requestBody) {
        // 如果请求体中有clientId字段，则使用它作为客户端标识
        if (requestBody.containsKey("clientId")) {
            return requestBody.get("clientId").toString();
        }
        // 否则使用IP地址作为标识
        return getClientIpFromRequest(requestBody);
    }

    /**
     * 更新Nginx配置中的luren.online server块
     */
    private void updateNginxServerBlock(String clientIp) throws IOException {
        // 读取现有配置文件
        Path confPath = Paths.get(NGINX_CONF_PATH);
        if (!Files.exists(confPath)) {
            throw new RuntimeException("Nginx configuration file not found: " + NGINX_CONF_PATH);
        }
        String nginxConf = new String(Files.readAllBytes(confPath));
        logger.debug("Original config length: {}", nginxConf.length());
        // 生成新的server块配置
        String newServerBlock = generateLurenOnlineServerBlock(clientIp);
        logger.debug("New server block:\n{}", newServerBlock);
        // 更新配置中的server块
        String updatedConf = updateServerBlock(nginxConf, newServerBlock);
        // 只有在配置确实发生变化时才写入文件
        if (!nginxConf.equals(updatedConf)) {
            logger.info("Config changed, writing to file");
            logger.debug("Updated config length: {}", updatedConf.length());
            // 写入更新后的配置
            Files.write(confPath, updatedConf.getBytes());
            logger.info("Nginx configuration updated successfully");
        } else {
            logger.info("Config unchanged, no need to write");
        }
    }

    /**
     * 生成luren.online的server块配置
     */
    private String generateLurenOnlineServerBlock(String clientIp) {
        return "server {\n" + "    listen 81;\n" + "    server_name luren.online;\n\n" + "    location / {\n" + "        proxy_pass http://[" + clientIp + "]:81;\n" + "        proxy_set_header Host $host;\n" + "        proxy_set_header X-Real-IP $remote_addr;\n" + "        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" + "    }\n" + "}";
    }

    /**
     * 更新配置中的server块 - 使用状态机解析器方法
     */
    private String updateServerBlock(String nginxConf, String newServerBlock) {
        try {
            logger.debug("Attempting to Find existing luren.online server block...");
            // 查找 server_name luren.online 的位置
            int serverNameIndex = nginxConf.indexOf("server_name luren.online;");
            if (serverNameIndex == -1) {
                logger.info("No existing luren.online server block found, trying to insert...");
                // 如果没有找到现有server块，则在指定位置添加新的server块
                return insertNewServerBlock(nginxConf, newServerBlock);
            }
            // 向后查找最近的 server { 或 server{ 开始位置
            int serverStart = findServerBlockStart(nginxConf, serverNameIndex);
            if (serverStart == -1) {
                logger.warn("Could not Find server block start, inserting new block");
                return insertNewServerBlock(nginxConf, newServerBlock);
            }
            // 查找server块的结束位置
            int serverEnd = findServerBlockEnd(nginxConf, serverStart);
            if (serverEnd == -1) {
                logger.warn("Could not Find server block end, inserting new block");
                return insertNewServerBlock(nginxConf, newServerBlock);
            }
            logger.info("Found existing luren.online server block, replacing...");
            logger.debug("Server block range: {} to {}", serverStart, serverEnd);
            // 提取现有的server块内容
            String existingBlock = nginxConf.substring(serverStart, serverEnd + 1);
            logger.debug("Existing block:\n{}", existingBlock);
            // 替换整个server块
            String before = nginxConf.substring(0, serverStart);
            String after = nginxConf.substring(serverEnd + 1);
            String result = before + newServerBlock + after;
            logger.debug("After replacement, validating config...");
            // 验证配置是否有效
            if (isValidConfig(result)) {
                logger.info("Replacement successful and config is valid");
                return result;
            } else {
                logger.warn("Replacement would create invalid config, returning original config");
                return nginxConf;
            }
        } catch (Exception e) {
            logger.error("Error updating server block: {}", e.getMessage(), e);
        }

        // 如果所有方法都失败，返回原始配置以避免破坏结构
        return nginxConf;
    }

    /**
     * 查找server块的开始位置
     */
    private int findServerBlockStart(String nginxConf, int serverNameIndex) {
        // 从server_name位置向前查找最近的server {或server{
        for (int i = serverNameIndex; i >= 0; i--) {
            // 检查是否匹配"server {"
            if (i >= 7 && "server {".equals(nginxConf.substring(i - 7, i + 1))) {
                return i - 7;
            }
            // 检查是否匹配"server{"
            if (i >= 6 && "server{".equals(nginxConf.substring(i - 6, i + 1))) {
                return i - 6;
            }
        }
        return -1;
    }

    /**
     * 查找server块的结束位置
     */
    private int findServerBlockEnd(String nginxConf, int serverStart) {
        int braceBalance = 0;

        for (int i = serverStart; i < nginxConf.length(); i++) {
            char c = nginxConf.charAt(i);

            if (c == '{') {
                braceBalance++;
            } else if (c == '}') {
                braceBalance--;
                // 当 braceBalance 回到 0 时说明当前 server 块结束
                if (braceBalance == 0) {
                    return i; // 找到匹配的结束大括号
                }
            }

            // 如果大括号结构非法（右括号多于左括号），提前退出
            if (braceBalance < 0) {
                return -1;
            }
        }

        return -1; // 没有找到匹配的结束大括号
    }

    /**
     * 插入新的server块到指定位置
     */
    private String insertNewServerBlock(String nginxConf, String newServerBlock) {
        String includeLine = "include /www/server/panel/vhost/nginx/*.conf;";
        int includeIndex = nginxConf.indexOf(includeLine);

        if (includeIndex != -1) {
            // 找到该行的结束位置并跳过换行符
            int lineEnd = nginxConf.indexOf('\n', includeIndex + includeLine.length());
            if (lineEnd == -1) {
                lineEnd = nginxConf.length();
            } else {
                lineEnd++; // 包含换行符
            }
            // 在该位置插入新的server块
            String before = nginxConf.substring(0, lineEnd);
            String after = nginxConf.substring(lineEnd);
            // 确保插入的内容前后有适当的换行
            String prefix = before.endsWith("\n") ? "" : "\n";
            String suffix = after.startsWith("\n") ? "" : "\n";


            return before + prefix + newServerBlock + "\n" + suffix + after;
        }

        return nginxConf;
    }

    /**
     * 验证配置是否有效
     */
    private boolean isValidConfig(String config) {
        // 计算大括号数量
        long openBraces = config.chars().filter(ch -> ch == '{').count();
        long closeBraces = config.chars().filter(ch -> ch == '}').count();

        // 检查大括号是否匹配
        if (openBraces != closeBraces) {
            logger.error("Brace mismatch: open={}, close={}", openBraces, closeBraces);
            return false;
        }
        // 更精确地检查配置结构
        int braceBalance = 0;
        for (char c : config.toCharArray()) {
            if (c == '{') {
                braceBalance++;
            } else if (c == '}') {
                braceBalance--;
                // 如果在任何时候右括号多于左括号，则配置无效
                if (braceBalance < 0) {
                    logger.error("Invalid brace structure: unexpected '}}'");
                    return false;
                }
            }
        }
        // 检查配置是否以http块正确结束
        if (!config.trim().endsWith("}")) {
            logger.error("Config doesn't end with closing brace");
            return false;
        }
        return true;
    }

    /**
     * 重载Nginx配置
     */
    private void reloadNginxConfig() throws IOException, InterruptedException {
        // 检查配置文件语法
        Process testProcess = new ProcessBuilder("nginx", "-t").start();
        int testResult = testProcess.waitFor();

        if (testResult != 0) {
            // 读取错误输出
            String errorMsg = new String(testProcess.getErrorStream().readAllBytes());
            logger.error("Nginx config test output: {}", errorMsg);
            throw new RuntimeException("Nginx configuration test failed: " + errorMsg);
        }

        // 重载Nginx配置
        Process reloadProcess = new ProcessBuilder("nginx", "-s", "reload").start();
        int reloadResult = reloadProcess.waitFor();

        if (reloadResult != 0) {
            // 读取错误输出
            String errorMsg = new String(reloadProcess.getErrorStream().readAllBytes());
            throw new RuntimeException("Failed to reload Nginx: " + errorMsg);
        }
        logger.info("Nginx configuration reloaded successfully");
    }
}
