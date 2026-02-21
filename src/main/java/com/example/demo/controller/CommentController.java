package com.example.demo.controller;

import com.example.demo.auth.mapper.UserInfoMapper;
import com.example.demo.mapper.ArticleResourcePathMapper;
import com.example.demo.model.Comment;
import com.example.demo.server.CommentService;
import org.example.text.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private ArticleResourcePathMapper articleResourcePathMapper;

    /**
     * 添加评论
     *
     * @param comment 评论对象
     * @return 添加结果
     */
    @Client(address = "/user/addComment", name = "addComment")
    @PostMapping("/user/addComment")
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody Comment comment) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (commentService.addComment(comment)) {
                result.put("code", 200);
                result.put("message", "评论添加成功");
                result.put("data", comment);
                articleResourcePathMapper.commentArticle(comment.getArticleId());
                return ResponseEntity.ok(result);
            }else {
                result.put("code", 500);
                result.put("message", "评论添加失败");
                return ResponseEntity.status(500).body(result);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "评论添加失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 添加回复
     * @param comment 回复对象
     * @return 添加结果
     */
    @Client(address = "/user/addReply", name = "addReply")
    @PostMapping("/user/addReply")
    public ResponseEntity<Map<String, Object>> addReply(@RequestBody Comment comment) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (commentService.addReply(comment)) {
                articleResourcePathMapper.commentArticle(comment.getArticleId());
                commentService.updateReplyCount(comment.getParentId(),1);
                result.put("code", 200);
                result.put("message", "回复添加成功");
                result.put("data", comment);
                return ResponseEntity.ok(result);
            }else {
                result.put("code", 500);
                result.put("message", "回复添加失败");
                return ResponseEntity.status(500).body(result);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "回复添加失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 获取文章的所有评论
     * @param articleId 文章ID
     * @return 评论列表
     */
    //这里
    @Client(address = "/article", name = "article")
    @GetMapping("/article")
    public ResponseEntity<Map<String, Object>> getCommentsByArticleId(@RequestParam Long articleId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Comment> comments = commentService.getCommentsByArticleId(articleId);
            result.put("code", 200);
            result.put("message", "获取评论成功");
            result.put("data", comments);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取评论失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
    /**
     * 获取回复评论
     * @param parentId 父评论ID
     * @return 评论列表
     */
    @Client(address = "/articleReply", name = "articleReply")
    @GetMapping("/articleReply")
    public ResponseEntity<Map<String, Object>> getReplyComments(@RequestParam Long parentId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Comment> comments = commentService.getReplyComments(parentId);
            result.put("code", 200);
            result.put("message", "获取回复评论成功");
            result.put("data", comments);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
        }
        return ResponseEntity.status(500).body(result);
    }
    /**
     * 删除评论
     *
     * @param timestamp 评论ID
     * @return 删除结果
     */
    @Client(address = "/user/deleteComment", name = "deleteComment")
    @GetMapping("/user/deleteComment")
    public ResponseEntity<Map<String, Object>> deleteComment(@RequestParam Long timestamp, @RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            //获取请求头中的
            //authorization Bearer eb962f1691be288098def8091251c58c8ca12ff342840e0759318250f7f6deb6
            token = token.replace("Bearer ", "");
            String email = userInfoMapper.getEmailByToken(token);
            Comment comment = commentService.getCommentById(timestamp);
            if (!email.equals(comment.getEmail())) {
                result.put("code", 403);
                result.put("message", "没有权限");
                return ResponseEntity.status(403).body(result);
            }

            int count = commentService.deleteComment(timestamp);
            articleResourcePathMapper.deleteCommentArticle(comment.getArticleId(), count);
            result.put("code", 200);
            result.put("message", "评论删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "评论删除失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

}
