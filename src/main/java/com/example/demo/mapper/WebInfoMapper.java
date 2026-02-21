package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WebInfoMapper {
    @Update("update web_info set page_views = page_views+1 where id = 1")
    void updatePageViews();
    @Select("select page_views from web_info where id = 1")
    long getPageViews();
}
