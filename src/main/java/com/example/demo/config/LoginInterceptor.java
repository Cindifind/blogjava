package com.example.demo.config;

import com.example.demo.mapper.WebInfoMapper;
import com.example.demo.util.IPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(LoginInterceptor.class);
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private WebInfoMapper webInfoMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        //将请求的ip地址存入redis中存放时间为一天
        String ip = new IPConfig().getClientIP(request);
        if (ip.length()>39 || ip.contains("$") || ip.contains("{")) {
            response.setContentType("application/json;charset=UTF-8");
            // 自定义返回体
            String jsonResponse = "{\"code\": 403, \"message\": \"Invalid IP address\"}";
            // 写入返回体
            response.getWriter().write(jsonResponse);
            return false;
        }
        //从redis中查找是否有此ip有的则返回false没有则插入
        if (redisTemplate.opsForValue().get("ip:" + ip) == null) {
            redisTemplate.opsForValue().set("ip:" + ip, ip, 24 * 60 * 60 * 30, java.util.concurrent.TimeUnit.SECONDS);
            webInfoMapper.updatePageViews();
            log.info("ip:" + ip + "访问成功");
        }
        return true;
    }
}
