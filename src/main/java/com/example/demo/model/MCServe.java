package com.example.demo.model;

public class MCServe {
    private String serverId;
    private String email;
    private String serverAddress;

    public MCServe() {
    }

    public MCServe(String serverId, String email, String serverAddress) {
        this.serverId = serverId;
        this.email = email;
        this.serverAddress = serverAddress;
    }

    /**
     * 获取
     * @return serverId
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * 设置
     * @param serverId
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
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
     * @return serverAddress
     */
    public String getServerAddress() {
        return serverAddress;
    }

    /**
     * 设置
     * @param serverAddress
     */
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String toString() {
        return "MCServeServe{serverId = " + serverId + ", email = " + email + ", serverAddress = " + serverAddress + "}";
    }
}
