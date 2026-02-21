package com.example.demo.model;

import java.util.Date;

public class Comment {
    private Long timestamp;
    private Long articleId;
    private String author;
    private String email;
    private String content;
    private Long parentId;
    private int reply;
    private String rootId;

    public Comment() {
    }

    public Comment(Long timestamp, Long articleId, String author, String email, String content, Long parentId, int reply, String rootId) {
        this.timestamp = timestamp;
        this.articleId = articleId;
        this.author = author;
        this.email = email;
        this.content = content;
        this.parentId = parentId;
        this.reply = reply;
        this.rootId = rootId;
    }

    /**
     * 获取
     * @return timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置
     * @param timestamp
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取
     * @return articleId
     */
    public Long getArticleId() {
        return articleId;
    }

    /**
     * 设置
     * @param articleId
     */
    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    /**
     * 获取
     * @return author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 设置
     * @param author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 获取
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取
     * @return content
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取
     * @return parentId
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * 设置
     * @param parentId
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * 获取
     * @return reply
     */
    public int getReply() {
        return reply;
    }

    /**
     * 设置
     * @param reply
     */
    public void setReply(int reply) {
        this.reply = reply;
    }

    /**
     * 获取
     * @return rootId
     */
    public String getRootId() {
        return rootId;
    }

    /**
     * 设置
     * @param rootId
     */
    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public String toString() {
        return "Comment{timestamp = " + timestamp + ", articleId = " + articleId + ", author = " + author + ", email = " + email + ", content = " + content + ", parentId = " + parentId + ", reply = " + reply + ", rootId = " + rootId + "}";
    }
}
