package com.example.demo.auth.model;

public class UserInfo {
    private String email;
    private String token;
    private int grade;
    private boolean isEnable;
    private String imgUrl;
    private String name;

    public UserInfo() {
    }

    public UserInfo( String email, String token, int grade, boolean isEnable, String imgUrl, String name) {
        this.email = email;
        this.token = token;
        this.grade = grade;
        this.isEnable = isEnable;
        this.imgUrl = imgUrl;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public boolean isIsEnable() {
        return isEnable;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
