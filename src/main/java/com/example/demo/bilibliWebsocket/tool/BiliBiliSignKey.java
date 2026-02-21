package com.example.demo.bilibliWebsocket.tool;

public class BiliBiliSignKey {
    private String appkey;
    private String secret;

    public BiliBiliSignKey(String appkey, String secret) {
        this.appkey = appkey;
        this.secret = secret;
    }

    public String getAppkey() { return appkey; }
    public String getSecret() { return secret; }
}