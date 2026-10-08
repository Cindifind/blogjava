package com.example.demo.auth.model;

public class AIInfo {
    private String name;
    private String description;
    private String model;
    private String key;

    public AIInfo() {
    }

    public AIInfo(String name, String description, String model, String key) {
        this.name = name;
        this.description = description;
        this.model = model;
        this.key = key;
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
     * @return model
     */
    public String getModel() {
        return model;
    }

    /**
     * 设置
     * @param model
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 获取
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }

    public String toString() {
        return "AIInfo{name = " + name + ", description = " + description + ", model = " + model + ", key = " + key + "}";
    }
}
