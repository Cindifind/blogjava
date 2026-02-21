package com.example.demo.model;

import org.springframework.stereotype.Component;

public class ApiStats {
    private String url;
    private int count;
    private String description;
    private String java;
    private String python;
    private String javascript;

    public ApiStats() {
    }

    public ApiStats(String url, int count, String description, String java, String python, String javascript) {
        this.url = url;
        this.count = count;
        this.description = description;
        this.java = java;
        this.python = python;
        this.javascript = javascript;
    }

    /**
     * 获取
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取
     * @return count
     */
    public int getCount() {
        return count;
    }

    /**
     * 设置
     * @param count
     */
    public void setCount(int count) {
        this.count = count;
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
     * @return java
     */
    public String getJava() {
        return java;
    }

    /**
     * 设置
     * @param java
     */
    public void setJava(String java) {
        this.java = java;
    }

    /**
     * 获取
     * @return python
     */
    public String getPython() {
        return python;
    }

    /**
     * 设置
     * @param python
     */
    public void setPython(String python) {
        this.python = python;
    }

    /**
     * 获取
     * @return javascript
     */
    public String getJavascript() {
        return javascript;
    }

    /**
     * 设置
     * @param javascript
     */
    public void setJavascript(String javascript) {
        this.javascript = javascript;
    }

    public String toString() {
        return "ApiStats{url = " + url + ", count = " + count + ", description = " + description + ", java = " + java + ", python = " + python + ", javascript = " + javascript + "}";
    }
}
