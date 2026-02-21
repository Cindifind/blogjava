package com.example.demo.server;

import com.example.demo.mapper.CommentMapper;
import com.example.demo.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    /**
     * 添加评论
     *
     * @param comment 评论对象
     * @return 添加的评论
     */
    public boolean addComment(Comment comment) {

        // 如果timestamp为空，使用当前时间戳
        if (comment.getTimestamp() == null) {
            comment.setTimestamp(System.currentTimeMillis());
        }
        return commentMapper.insertComment(comment);
    }

    public boolean addReply(Comment comment) {
        // 如果timestamp为空，使用当前时间戳
        if (comment.getTimestamp() == null) {
            comment.setTimestamp(System.currentTimeMillis());
        }
        Long parentId = comment.getParentId();
        Long articleId = comment.getArticleId();
        Comment commentById = commentMapper.findCommentById(parentId);
        return commentMapper.addReply(comment);
    }

    public void updateReplyCount(Long timestamp,int count) {
        commentMapper.updateReplyCount(timestamp, count);
    }

    public List<Comment> getReplyComments(Long parentId) {
        return commentMapper.findReplyByParentId(parentId);
    }

    /**
     * 获取文章的所有评论
     *
     * @param articleId 文章ID
     * @return 评论列表
     */
    public List<Comment> getCommentsByArticleId(Long articleId) {
        return commentMapper.findCommentsByArticleId(articleId);
    }

    /**
     * 根据ID获取评论
     *
     * @param timestamp 评论ID
     * @return 评论对象
     */
    public Comment getCommentById(Long timestamp) {
        return commentMapper.findCommentById(timestamp);
    }

    /**
     * 删除评论
     *
     * @param timestamp 评论ID
     */
    public int deleteComment(Long timestamp) {
        List<Long> parentIds = commentMapper.findParentId(timestamp);
        Comment comment = commentMapper.findCommentById(timestamp);
        Long parentId = comment.getParentId();
        if (parentId != null) commentMapper.updateReplyCount(parentId, -parentIds.size()-1);
        if (!parentIds.isEmpty()) commentMapper.deleteComments(parentIds);
        commentMapper.deleteComment(timestamp);
        return parentIds.size()+1;
    }

    /**
     * 删除文章的所有评论
     *
     * @param articleId 文章ID
     */
    public void deleteCommentsByArticleId(Long articleId) {
        commentMapper.deleteCommentsByArticleId(articleId);
    }

    /**
     * 获取文章的评论数量
     *
     * @param articleId 文章ID
     * @return 评论数量
     */
    public int getCommentCountByArticleId(Long articleId) {
        return commentMapper.countCommentsByArticleId(articleId);
    }

    /**
     * 获取总评论数
     *
     * @return 总评论数
     */
    public int getTotalCommentCount() {
        return commentMapper.getTotalCommentCount();
    }
}
