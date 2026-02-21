package com.example.demo.bilibliWebsocket.map;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Host {
    private String host;
    private int port;

    @JsonProperty("wss_port")
    private int wssPort;

    @JsonProperty("ws_port")
    private int wsPort;

    public Host() {
    }

    public Host(String host, int port, int wssPort, int wsPort) {
        this.host = host;
        this.port = port;
        this.wssPort = wssPort;
        this.wsPort = wsPort;
    }

    /**
     * 获取
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置
     * @param host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 获取
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * 设置
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获取
     * @return wssPort
     */
    public int getWssPort() {
        return wssPort;
    }

    /**
     * 设置
     * @param wssPort
     */
    public void setWssPort(int wssPort) {
        this.wssPort = wssPort;
    }

    /**
     * 获取
     * @return wsPort
     */
    public int getWsPort() {
        return wsPort;
    }

    /**
     * 设置
     * @param wsPort
     */
    public void setWsPort(int wsPort) {
        this.wsPort = wsPort;
    }

    public String toString() {
        return "Host{host = " + host + ", port = " + port + ", wssPort = " + wssPort + ", wsPort = " + wsPort + "}";
    }
}
