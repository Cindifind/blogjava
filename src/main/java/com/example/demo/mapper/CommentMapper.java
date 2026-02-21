package com.example.demo.mapper;

import com.example.demo.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("INSERT INTO comments(timestamp, article_id, email, content)VALUES(#{timestamp}, #{articleId}, #{email}, #{content})")
    boolean insertComment(Comment comment);

    @Select("SELECT * FROM comments WHERE article_id = #{articleId} and parent_id IS NULL")
    List<Comment> findCommentsByArticleId(Long articleId);

    @Select("SELECT * FROM comments WHERE timestamp = #{timestamp}")
    @Result(property = "articleId", column = "article_id")
    @Result(property = "parentId", column = "parent_id")
    Comment findCommentById(Long timestamp);

    @Delete("DELETE FROM comments WHERE timestamp = #{timestamp}")
    void deleteComment(Long timestamp);

    @Delete("DELETE FROM comments WHERE article_id = #{articleId}")
    void deleteCommentsByArticleId(Long articleId);

    @Select("SELECT COUNT(*) FROM comments WHERE article_id = #{articleId}")
    int countCommentsByArticleId(Long articleId);

    @Select("SELECT COUNT(*) FROM comments")
    int getTotalCommentCount();
    //添加回复
    @Insert("INSERT INTO comments(timestamp,article_id, email, content, parent_id) VALUES (#{timestamp},#{articleId}, #{email}, #{content}, #{parentId})")
    boolean addReply(Comment comment);
    //查询回复
    @Select("SELECT * FROM comments WHERE parent_id = #{timestamp}")
    List<Comment> findReplyByParentId(Long timestamp);
    //增加父评论回复数量
    @Update("UPDATE comments SET reply = reply + #{count} WHERE timestamp = #{timestamp}")
    void updateReplyCount(Long timestamp, int count);
    //查询父评论id
    @Select("SELECT timestamp FROM comments WHERE parent_id = #{timestamp}")
    List<Long> findParentId(Long timestamp);
    //批量删除评论
    void deleteComments(List<Long> list);
}