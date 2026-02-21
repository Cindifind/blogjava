package com.example.demo.mapper;

import com.example.demo.model.ImageRecords;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ImageRecordsMapper {
    @Insert("INSERT INTO image_records(timestamp, mdImage) VALUES(#{timestamp}, #{mdImage})")
    void insertImageRecords(ImageRecords imageRecords);
    @Select("SELECT * FROM image_records WHERE mdImage = #{mdImage}")
    ImageRecords findImageRecordsById(String mdImage);
    @Select("SELECT * FROM image_records")
    List<ImageRecords> findAllImageRecords();
    @Delete("DELETE FROM image_records WHERE mdImage = #{mdImage}")
    void deleteImageRecords(String mdImage);
    //获取是否有此图片
    @Select("SELECT mdImage FROM image_records WHERE mdImage = #{mdImage}")
    String getImage(String mdImage);
}
