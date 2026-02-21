package com.example.demo.mapper;

import com.example.demo.model.ArticleResourcePath;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ArticleResourcePathMapper {

    @Insert("INSERT INTO article_resource_path (timestamp, image, md, email,description,title,label,`like`,comment,browse) VALUES (#{timestamp}, #{image}, #{md}, #{email},#{description},#{title},#{label},#{like},#{comment},#{browse})")
    void insertArticleResourcePath(ArticleResourcePath articleResourcePath);

    @Delete("DELETE FROM article_resource_path WHERE timestamp = #{timestamp}")
    void deleteArticleResourcePath(long timestamp);

    @Select("SELECT COUNT(*) FROM article_resource_path")
    int getArticleCount();
    @Select("SELECT image,md FROM article_resource_path")
    List<ArticleResourcePath> findAllArticleStatic();
    @Select("SELECT * FROM article_resource_path ORDER BY timestamp DESC LIMIT #{pageNum}, 20")
    List<ArticleResourcePath> findArticleByPageNum(int pageNum);

    @Select("SELECT * FROM article_resource_path WHERE timestamp = #{timestamp}")
    List<ArticleResourcePath> findArticleByTimestamp(long timestamp);

    //点赞
    @Update("UPDATE article_resource_path SET `like` = `like` + 1 WHERE timestamp = #{timestamp}")
    void likeArticle(long timestamp);

    //评论
    @Update("UPDATE article_resource_path SET comment = comment + 1 WHERE timestamp = #{timestamp}")
    void commentArticle(long timestamp);

    //删除评论
//    @Update("UPDATE article_resource_path SET comment = comment - 1 WHERE timestamp = #{timestamp}")
//    void deleteCommentArticle(long timestamp);
    @Update("UPDATE article_resource_path SET comment = comment - #{count} WHERE timestamp = #{timestamp}")
    void deleteCommentArticle(long timestamp,int count);

    //获取是否有此图片
    @Select("SELECT image from article_resource_path WHERE image = #{image}")
    String getImage(String image);

    //获取是否有此文章
    @Select("SELECT md from article_resource_path WHERE md = #{md}")
    String getMd(String md);
}