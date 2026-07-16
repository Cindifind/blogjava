package com.example.demo.auth.mapper;
import com.example.demo.auth.model.UserInfo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserInfoMapper{
    @Select("select * from user_info where password=#{password}")
    @Result(property = "isEnable", column = "is_Enable") // 关键：明确映射
    UserInfo getUserInfoByPassword(String password);
    @Insert("insert into user_info(email,password,grade,is_Enable,salt) values(#{email},#{password},#{grade},#{isEnable},#{salt})")
    void insertUserInfo(UserInfo userInfo);
    @Select("select email from user_info where email= #{email}")
    String getEmail(String email);
    @Update("update user_info set password=#{password},salt=#{salt} where email=#{email}")
    void updateUserInfoPassword(String email, String password, String salt);
    @Select("select email from user_info where password= #{password}")
    String getEmailByPassword(String password);
    @Select("select salt from user_info where email= #{email}")
    String getSalt(String email);
    @Select("select * from user_info where email= #{email}")
    @Result(property = "isEnable", column = "is_Enable") // 关键：明确映射
    UserInfo getUserInfoByEmail(String email);
}
