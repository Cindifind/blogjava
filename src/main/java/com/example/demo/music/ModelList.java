package com.example.demo.music;

public class ModelList {
    private String id;
    private String name;
    private String artistsname;
    private String picurl;
    private String musicDuration;

    public ModelList() {
    }

    public ModelList(String id, String name, String artistsname, String picurl, String musicDuration) {
        this.id = id;
        this.name = name;
        this.artistsname = artistsname;
        this.picurl = picurl;
        this.musicDuration = musicDuration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getArtistsname() {
        return artistsname;
    }
    public void setArtistsname(String artistsname) {
        this.artistsname = artistsname;
    }

    public String getPicurl() {
        return picurl;
    }
    public void setPicurl(String picurl) {
        this.picurl = picurl;
    }

    public String getMusicDuration() {
        return musicDuration;
    }

    public void setMusicDuration(String musicDuration) {
        this.musicDuration = musicDuration;
    }

    public String toString() {
        return "{id = " + id + ", name = " + name + ", artistsname = " + artistsname + ", picurl = " + picurl + ", musicDuration = " + musicDuration + "}";
    }
}
