package com.example.demo.mapper;

import com.example.demo.model.Config;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

public interface ConfigMapper {
    @Insert("INSERT INTO config (`key`, value) VALUES (#{key}, #{value})")
    void insert(Config config);
    @Insert("UPDATE config SET value = #{value} WHERE `key` = #{key}")
    void update(Config config);
    @Delete("DELETE FROM config WHERE `key` = #{key}")
    void delete(Config config);
}
