package com.example.demo.bilibliWebsocket.map;


import java.nio.ByteBuffer;

public class Package {
    long uid;
    long roomid;
    long protover;
    String buvid;
    String platform;
    long type;
    String key;

    public Package() {
    }

    public Package(long uid, long roomid, long protover, String buvid, String platform, long type, String key) {
        this.uid = uid;
        this.roomid = roomid;
        this.protover = protover;
        this.buvid = buvid;
        this.platform = platform;
        this.type = type;
        this.key = key;
    }

    /**
     * 创建一个数据包
     *
     * @param operation 操作符
     * @param data      数据
     * @return 返回一个包
     */
    public static byte[] createDataPackage(int operation, byte[] data) {
        int length = data.length + 16;
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putInt(length);
        buffer.putShort((short) 16);
        buffer.putShort((short) 1);
        buffer.putInt(operation);
        buffer.putInt(1);
        buffer.put(data);
        return buffer.array();
    }

    /**
     * 获取
     * @return uid
     */
    public long getUid() {
        return uid;
    }

    /**
     * 设置
     * @param uid
     */
    public void setUid(long uid) {
        this.uid = uid;
    }

    /**
     * 获取
     * @return roomid
     */
    public long getRoomid() {
        return roomid;
    }

    /**
     * 设置
     * @param roomid
     */
    public void setRoomid(long roomid) {
        this.roomid = roomid;
    }

    /**
     * 获取
     * @return protover
     */
    public long getProtover() {
        return protover;
    }

    /**
     * 设置
     * @param protover
     */
    public void setProtover(long protover) {
        this.protover = protover;
    }

    /**
     * 获取
     * @return buvid
     */
    public String getBuvid() {
        return buvid;
    }

    /**
     * 设置
     * @param buvid
     */
    public void setBuvid(String buvid) {
        this.buvid = buvid;
    }

    /**
     * 获取
     * @return platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * 设置
     * @param platform
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * 获取
     * @return type
     */
    public long getType() {
        return type;
    }

    /**
     * 设置
     * @param type
     */
    public void setType(long type) {
        this.type = type;
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
        return "Package{uid = " + uid + ", roomid = " + roomid + ", protover = " + protover + ", buvid = " + buvid + ", platform = " + platform + ", type = " + type + ", key = " + key + "}";
    }
}
