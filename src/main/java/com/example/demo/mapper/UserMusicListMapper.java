package com.example.demo.mapper;

import com.example.demo.model.UserMusicList;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMusicListMapper {
    @Select("select music_list from user_music where email =#{email}")
    String selectMusicList(String email);
    @Insert("insert into user_music(email,music_list) values(#{email},#{musicList})")
    int insertMusicList(UserMusicList userMusicList);
    @Update("update user_music set music_list = #{musicList} where email = #{email}")
    int updateMusicList(UserMusicList userMusicList);
}
