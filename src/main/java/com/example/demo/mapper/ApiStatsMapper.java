package com.example.demo.mapper;

import com.example.demo.model.ApiStats;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApiStatsMapper {
    @Insert("insert into api_stats(url) values(#{apiName})")
    void insertApiStats(String apiName);

    @Select("select * from api_stats where url = #{apiName}")
    ApiStats getApiStats(String apiName);

    @Update("update api_stats set count = count+1 where url = #{apiName}")
    void updateApiStatsCount(String apiName);

    @Update("update api_stats set description = #{description},java = #{java},python = #{python},javascript=#{javascript}  where url = #{apiName}")
    void updateApiStatsDescription(String apiName, String description, String java, String python, String javascript);

    @Select("select * from api_stats")
    List<ApiStats> getAllApiStats();

    @Delete( "delete from api_stats where url = #{apiName}")
    void deleteApiStats(String apiName);
    
    // 移除原来的注解方式，改用 XML 映射文件中的 foreach 实现
    List<ApiStats> getApiList(@Param("apiNameList") List<String> apiNameList);
}