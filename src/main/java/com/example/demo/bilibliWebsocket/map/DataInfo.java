package com.example.demo.bilibliWebsocket.map;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DataInfo {
    private String group;

    @JsonProperty("business_id")
    private int businessId;

    @JsonProperty("refresh_row_factor")
    private double refreshRowFactor;

    @JsonProperty("refresh_rate")
    private int refreshRate;

    @JsonProperty("max_delay")
    private int maxDelay;

    private String token;


    @JsonProperty("host_list")
    private List<Host> hostList;

    public DataInfo() {
    }

    public DataInfo(String group, int businessId, double refreshRowFactor, int refreshRate, int maxDelay, String token, List<Host> hostList) {
        this.group = group;
        this.businessId = businessId;
        this.refreshRowFactor = refreshRowFactor;
        this.refreshRate = refreshRate;
        this.maxDelay = maxDelay;
        this.token = token;
        this.hostList = hostList;
    }

    /**
     * 获取
     * @return group
     */
    public String getGroup() {
        return group;
    }

    /**
     * 设置
     * @param group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 获取
     * @return businessId
     */
    public int getBusinessId() {
        return businessId;
    }

    /**
     * 设置
     * @param businessId
     */
    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    /**
     * 获取
     * @return refreshRowFactor
     */
    public double getRefreshRowFactor() {
        return refreshRowFactor;
    }

    /**
     * 设置
     * @param refreshRowFactor
     */
    public void setRefreshRowFactor(double refreshRowFactor) {
        this.refreshRowFactor = refreshRowFactor;
    }

    /**
     * 获取
     * @return refreshRate
     */
    public int getRefreshRate() {
        return refreshRate;
    }

    /**
     * 设置
     * @param refreshRate
     */
    public void setRefreshRate(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    /**
     * 获取
     * @return maxDelay
     */
    public int getMaxDelay() {
        return maxDelay;
    }

    /**
     * 设置
     * @param maxDelay
     */
    public void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }

    /**
     * 获取
     * @return token
     */
    public String getToken() {
        return token;
    }

    /**
     * 设置
     * @param token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 获取
     * @return hostList
     */
    public List<Host> getHostList() {
        return hostList;
    }

    /**
     * 设置
     * @param hostList
     */
    public void setHostList(List<Host> hostList) {
        this.hostList = hostList;
    }

    public String toString() {
        return "DataInfo{group = " + group + ", businessId = " + businessId + ", refreshRowFactor = " + refreshRowFactor + ", refreshRate = " + refreshRate + ", maxDelay = " + maxDelay + ", token = " + token + ", hostList = " + hostList + "}";
    }
}
