package com.example.demo.model;

public class ImageRecords {
    private long timestamp;
    private String mdImage;

    public ImageRecords() {
    }

    public ImageRecords(long timestamp, String mdImage) {
        this.timestamp = timestamp;
        this.mdImage = mdImage;
    }

    /**
     * 获取
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 设置
     * @param timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取
     * @return mdImage
     */
    public String getMdImage() {
        return mdImage;
    }

    /**
     * 设置
     * @param mdImage
     */
    public void setMdImage(String mdImage) {
        this.mdImage = mdImage;
    }

    public String toString() {
        return "ImageRecords{timestamp = " + timestamp + ", mdImage = " + mdImage + "}";
    }
}
