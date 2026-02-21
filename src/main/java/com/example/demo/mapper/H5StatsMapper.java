package com.example.demo.mapper;

import com.example.demo.model.H5Stats;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface H5StatsMapper {
    @Select("select * from h5_stats")
    List<H5Stats> getAllH5Stats();
    @Insert("insert into h5_stats(url,description,api,name) values(#{url},#{description},#{api},#{name})")
    int insertH5Stats(H5Stats h5Stats);
    //更新api
    @Update("update h5_stats set url = #{updateUrl}, api = #{api}, name = #{name}, description = #{description} where url = #{url}")
    int updateH5StatsApi(String api,String name, String description ,String url ,String updateUrl);

    @Delete("delete from h5_stats where url = #{url}")
    int deleteH5Stats(String url);
    @Select("select api from h5_stats where url = #{url}")
    String getApiByUrl(String url);
}