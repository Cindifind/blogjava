package com.example.demo.auth.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class UserInfo {
    private String email;
    private int grade;
    private boolean isEnable;
    private String imgUrl;
    private String name;
    private String salt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;
    private String refreshToken;
    private String accessToken;

    public UserInfo() {
    }

    public UserInfo(String email, int grade, boolean isEnable, String imgUrl, String name, String salt, String password, String refreshToken, String accessToken) {
        this.email = email;
        this.grade = grade;
        this.isEnable = isEnable;
        this.imgUrl = imgUrl;
        this.name = name;
        this.salt = salt;
        this.password = password;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
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
     * @return grade
     */
    public int getGrade() {
        return grade;
    }

    /**
     * 设置
     * @param grade
     */
    public void setGrade(int grade) {
        this.grade = grade;
    }

    /**
     * 获取
     * @return isEnable
     */
    public boolean isIsEnable() {
        return isEnable;
    }

    /**
     * 设置
     * @param isEnable
     */
    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    /**
     * 获取
     * @return imgUrl
     */
    public String getImgUrl() {
        return imgUrl;
    }

    /**
     * 设置
     * @param imgUrl
     */
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    /**
     * 获取
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取
     * @return salt
     */
    public String getSalt() {
        return salt;
    }

    /**
     * 设置
     * @param salt
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * 获取
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取
     * @return refreshToken
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * 设置
     * @param refreshToken
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * 获取
     * @return accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 设置
     * @param accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String toString() {
        return "UserInfo{email = " + email + ", grade = " + grade + ", isEnable = " + isEnable + ", imgUrl = " + imgUrl + ", name = " + name + ", salt = " + salt + ", password = " + password + ", refreshToken = " + refreshToken + ", accessToken = " + accessToken + "}";
    }
}
