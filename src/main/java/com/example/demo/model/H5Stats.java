package com.example.demo.model;

public class H5Stats {
    private String url;
    private String name;
    private String description;
    private String api;

    public H5Stats() {
    }

    public H5Stats(String url, String name, String description, String api) {
        this.url = url;
        this.name = name;
        this.description = description;
        this.api = api;
    }

    /**
     * 获取
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置url
     * @param url url地址
     */
    public void setUrl(String url) {
        this.url = url;
    }
    /**
     * 获取
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * 设置name
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置description
     * @param description 描述信息
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取
     * @return api
     */
    public String getApi() {
        return api;
    }

    /**
     * 设置api
     * @param api 接口地址
     */
    public void setApi(String api) {
        this.api = api;
    }

    public String toString() {
        return "H5Stats{url = " + url + ", name = " + name + ", description = " + description + ", api = " + api + "}";
    }
}