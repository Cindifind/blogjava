package com.example.demo.auth.server;

import com.example.demo.util.IPConfig;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class VerificationCodeService {
    private static final Logger log = LoggerFactory.getLogger(VerificationCodeService.class);
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String IP_PREFIX = "ip_limit:";
    private static final int IP_LIMIT_EXPIRE_TIME = 60; // 1分钟
    private static final String CODE_PREFIX = "verification_code:";
    private static final int CODE_EXPIRE_TIME = 300; // 5分钟
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    /**
     * 发送验证码
     */
    public ResponseEntity<Map<String, Object>> sendVerificationCode(String email, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 获取客户端IP地址
            String ip = new IPConfig().getClientIP(request);

            // 检查IP频率限制
            String ipKey = IP_PREFIX + ip;
            String storedIp = redisTemplate.opsForValue().get(ipKey);
            if (storedIp != null) {
                log.warn("IP {} 在1分钟内已请求过验证码，请勿重复请求", ip);
                response.put("message", "请求过于频繁，请稍后再试");
                response.put("code", 429);
                return ResponseEntity.status(429).body(response); // 429 Too Many Requests
            }
            // 验证邮箱格式
            if (email == null || !isValidEmail(email)) {
                log.warn("无效的邮箱地址: {}", email);
                response.put("message", "邮箱地址格式不正确");
                return ResponseEntity.badRequest().body(response);
            }
            String key = CODE_PREFIX + email;
            // 先检查是否存在未过期的验证码
            String storedCode = redisTemplate.opsForValue().get(key);
            if (storedCode != null) {
                // 获取剩余过期时间
                Long expireTime = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                // 如果剩余时间小于等于4分钟(240秒)，即已存在超过1分钟，允许重新发送
                if (expireTime <= 240) {
                    // 删除之前的验证码
                    redisTemplate.delete(key);
                } else {
                    log.warn("邮箱 {} 的验证码存在且未超过1分钟，请勿重复发送", email);
                    response.put("message", "验证码已发送，请稍后再试");
                    response.put("code", 429);
                    return ResponseEntity.status(429).body(response); // 429 Too Many Requests
                }
            }

            // 生成6位随机验证码
            String code = generateVerificationCode();
            log.debug("为邮箱 {} 生成验证码: {}", email, code);

            // 使用SET命令的NX选项原子性地设置验证码（只有当key不存在时才设置）
            Boolean setResult = redisTemplate.opsForValue().setIfAbsent(key, code, CODE_EXPIRE_TIME, TimeUnit.SECONDS);

            // 设置IP限制
            redisTemplate.opsForValue().set(ipKey, "0", IP_LIMIT_EXPIRE_TIME, TimeUnit.SECONDS);

            if (setResult != null && setResult) {
                log.debug("验证码已存储到Redis，邮箱: {}, 过期时间: {}秒", email, CODE_EXPIRE_TIME);
                // 发送邮件
                ResponseEntity<Map<String, Object>> result = sendEmail(email, code);
                if (result.getStatusCode().is2xxSuccessful()) {
                    log.info("验证码发送成功，邮箱: {}", email);
                    return result;
                } else {
                    log.warn("验证码发送失败，邮箱: {}", email);
                    // 发送失败时删除验证码和IP限制
                    redisTemplate.delete(key);
                    redisTemplate.delete(ipKey);
                    response.put("message", "验证码发送失败");
                    response.put("code", 500);
                    return ResponseEntity.status(500).body(response);
                }
            } else {
                log.warn("邮箱 {} 的验证码存在且未超过1分钟，请勿重复发送", email);
                response.put("message", "验证码已发送，请稍后再试");
                response.put("code", 429);
                return ResponseEntity.status(429).body(response); // 429 Too Many Requests
            }
        } catch (Exception e) {
            log.error("生成或存储验证码失败，邮箱: {}", email, e);
            response.put("message", "服务器内部错误");
            response.put("code", 500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 验证验证码
     */
    public ResponseEntity<Map<String, Object>> verifyCode(String email, String code, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String ip = new IPConfig().getClientIP(request);
            String storedCode = redisTemplate.opsForValue().get(CODE_PREFIX + email);
            String count = redisTemplate.opsForValue().get(IP_PREFIX + ip);
            int countInt = count == null ? 0 : Integer.parseInt(count);

            countInt++;

            // 更新尝试次数
            redisTemplate.opsForValue().set(IP_PREFIX + ip, Integer.toString(countInt), IP_LIMIT_EXPIRE_TIME, TimeUnit.SECONDS);

            if (countInt > 5) {
                // 直接设置1天的过期时间，不需要先设置1分钟再修改
                redisTemplate.opsForValue().set(IP_PREFIX + ip, Integer.toString(countInt), 1, TimeUnit.DAYS);
                log.warn("IP {} 该ip一被拉黑1天", ip);
                response.put("message", "尝试次数过多，IP已被限制");
                response.put("code", 403);
                return ResponseEntity.status(403).body(response);
            }

            if (storedCode != null && storedCode.equals(code)) {
                // 验证成功后删除验证码和IP限制
                redisTemplate.delete(CODE_PREFIX + email);
                redisTemplate.delete(IP_PREFIX + ip);
                log.debug("验证码验证成功并已删除，邮箱: {}", email);
                response.put("message", "验证码验证成功");
                response.put("code", 200);
                return ResponseEntity.ok(response);
            }
            log.debug("验证码验证失败，邮箱: {}, 输入验证码: {}, 存储验证码: {}", email, code, storedCode);
            response.put("message", "验证码错误");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("验证码验证过程中发生错误，邮箱: {}", email, e);
            response.put("message", "服务器内部错误");
            response.put("code", 500);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 发送邮件
     */
    private ResponseEntity<Map<String, Object>> sendEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("834888615@qq.com");
            helper.setTo(email);
            helper.setSubject("博客登录验证码");
            helper.setText("您的登录验证码是: " + code + "，5分钟内有效。", true);

            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人: {}", email);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "验证码邮件发送成功");
            response.put("code", 200);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("发送邮件失败，收件人: " + email, e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "发送邮件失败: " + e.getMessage());
            response.put("code", 500);
            return ResponseEntity.status(500).body(response);
        }
    }


    /**
     * 生成6位随机验证码
     */
    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

}
