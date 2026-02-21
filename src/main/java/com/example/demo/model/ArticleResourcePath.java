package com.example.demo.model;

public class ArticleResourcePath {
    private long timestamp;
    private String image;
    private String md;
    private String author;
    private String title;
    private String label;
    private String description;
    private String email;
    private long like;
    private long comment;
    private long browse;

    public ArticleResourcePath() {
    }

    public ArticleResourcePath(long timestamp, String image, String md, String author, String title, String label, String description, String email, long like, long comment, long browse) {
        this.timestamp = timestamp;
        this.image = image;
        this.md = md;
        this.author = author;
        this.title = title;
        this.label = label;
        this.description = description;
        this.email = email;
        this.like = like;
        this.comment = comment;
        this.browse = browse;
    }

    /**
     * 获取
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置
     * @param timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取
     * @return image
     */
    public String getImage() {
        return image;
    }

    /**
     * 设置
     * @param image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * 获取
     * @return md
     */
    public String getMd() {
        return md;
    }

    /**
     * 设置
     * @param md
     */
    public void setMd(String md) {
        this.md = md;
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
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取
     * @return label
     */
    public String getLabel() {
        return label;
    }

    /**
     * 设置
     * @param label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * 获取
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
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
     * @return like
     */
    public long getLike() {
        return like;
    }

    /**
     * 设置
     * @param like
     */
    public void setLike(long like) {
        this.like = like;
    }

    /**
     * 获取
     * @return comment
     */
    public long getComment() {
        return comment;
    }

    /**
     * 设置
     * @param comment
     */
    public void setComment(long comment) {
        this.comment = comment;
    }

    /**
     * 获取
     * @return browse
     */
    public long getBrowse() {
        return browse;
    }

    /**
     * 设置
     * @param browse
     */
    public void setBrowse(long browse) {
        this.browse = browse;
    }

    public String toString() {
        return "ArticleResourcePath{timestamp = " + timestamp + ", image = " + image + ", md = " + md + ", author = " + author + ", title = " + title + ", label = " + label + ", description = " + description + ", email = " + email + ", like = " + like + ", comment = " + comment + ", browse = " + browse + "}";
    }
}
