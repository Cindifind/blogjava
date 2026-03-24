package com.example.demo.auth.mapper;
import com.example.demo.auth.model.UserInfo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserInfoMapper{
    @Select("select * from user_info where token=#{token}")
    @Result(property = "isEnable", column = "is_Enable") // 关键：明确映射
    UserInfo getUserInfoByToken(String token);
    @Insert("insert into user_info(email,token,grade,is_Enable,salt) values(#{email},#{token},#{grade},#{isEnable},#{salt})")
    void insertUserInfo(UserInfo userInfo);
    @Select("select email from user_info where email= #{email}")
    String getEmail(String email);
    @Update("update user_info set token=#{token},salt=#{salt} where email=#{email}")
    void updateUserInfoPassword(String email, String token, String salt);
    @Select("select email from user_info where token= #{token}")
    String getEmailByToken(String token);
    @Select("select salt from user_info where email= #{email}")
    String getSalt(String email);
}
