package com.example.demo.server;

import com.example.demo.mapper.ApiStatsMapper;
import com.example.demo.model.ApiStats;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiUrlServer {
    //获取请求的url
    @Autowired
    private ApiStatsMapper apiStatsMapper;

    public void UpDataaApiState(HttpServletRequest request) {
        String apiName = request.getRequestURI();
        ApiStats apiStats = apiStatsMapper.getApiStats(apiName);
        if (apiStats == null) {
            apiStatsMapper.insertApiStats(apiName);
        } else {
            apiStatsMapper.updateApiStatsCount(apiName);
        }
    }

}
