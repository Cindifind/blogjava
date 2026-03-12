package com.example.demo.model;

public class UserMusicList {
    private String email;
    private String musicList;

    public UserMusicList() {
    }

    public UserMusicList(String email, String musicList) {
        this.email = email;
        this.musicList = musicList;
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
     * @return musicList
     */
    public String getMusicList() {
        return musicList;
    }

    /**
     * 设置
     * @param musicList
     */
    public void setMusicList(String musicList) {
        this.musicList = musicList;
    }

    public String toString() {
        return "UserMusicList{email = " + email + ", musicList = " + musicList + "}";
    }
}
