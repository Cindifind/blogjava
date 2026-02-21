package com.example.demo.bilibliWebsocket.map;

public class Address {
    private int code;
    private String message;
    private int ttl;
    private DataInfo data;

    public Address() {
    }

    public Address(int code, String message, int ttl, DataInfo data) {
        this.code = code;
        this.message = message;
        this.ttl = ttl;
        this.data = data;
    }

    /**
     * 获取
     * @return code
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置
     * @param code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取
     * @return ttl
     */
    public int getTtl() {
        return ttl;
    }

    /**
     * 设置
     * @param ttl
     */
    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    /**
     * 获取
     * @return data
     */
    public DataInfo getData() {
        return data;
    }

    /**
     * 设置
     * @param data
     */
    public void setData(DataInfo data) {
        this.data = data;
    }

    public String toString() {
        return "Address{code = " + code + ", message = " + message + ", ttl = " + ttl + ", data = " + data + "}";
    }
}
