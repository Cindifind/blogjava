package com.example.demo.mapper;

import com.example.demo.model.MCServe;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MCServeMapper {
    @Insert("INSERT INTO mc_serve (server_id,email, server_address) VALUES (#{serverId}, #{email}, #{serverAddress})")
    int insertMCServe(String serverId, String email, String serverAddress);

    @Delete("DELETE FROM mc_serve WHERE server_id = #{serverId} AND email = #{email}")
    int deleteMCServe(String serverId, String email);

    @Update("UPDATE mc_serve SET server_address = #{serverAddress} WHERE server_id = #{serverId} AND email = #{email}")
    int updateMCServe(String serverId, String email, String serverAddress);
    @Select("SELECT * FROM mc_serve WHERE server_id = #{serverId}")
    @Result(property = "serverAddress", column = "server_address")
    @Result(property = "serverId", column = "server_id")
    MCServe selectMCServe(String serverId);
    @Result(property = "serverAddress", column = "server_address")
    @Result(property = "serverId", column = "server_id")
    @Select("SELECT * FROM mc_serve")
    List<MCServe> selectAllMCServe();
    @Result(property = "serverAddress", column = "server_address")
    @Result(property = "serverId", column = "server_id")
    @Select("SELECT * FROM mc_serve WHERE email = #{email}")
    List<MCServe> selectMCServeByEmail(String email);
}
