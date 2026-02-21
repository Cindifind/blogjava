package com.example.demo.config;

import com.example.demo.auth.mapper.UserInfoMapper;
import com.example.demo.auth.model.UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserInterceptor implements HandlerInterceptor {
    @Autowired
    private UserInfoMapper userInfoMapper;
    private static final Logger log = LoggerFactory.getLogger(UserInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null){
            token = request.getParameter("token");
            if (token == null){
                //返回403
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"code\":403, \"message\":\"Please log in to use\"}");
                return false;
            }
        }
        token = token.replace("Bearer ", "");
        UserInfo userInfo = userInfoMapper.getUserInfoByToken(token);
        System.out.println(userInfo);
        if (userInfo == null) {
            log.info("用户不存在");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"code\":403, \"message\":\"User does not exist\"}");
            return false;
        }
        if (userInfo.getGrade() < 1 || !userInfo.isIsEnable()) {
            log.info("用户权限不足或账户已被禁用");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"code\":403, \"message\":\"Insufficient user permissions or account has been disabled\"}");
            return false;
        }
        log.info("用户权限正常"+userInfo.getEmail());
        return true;
    }
}
