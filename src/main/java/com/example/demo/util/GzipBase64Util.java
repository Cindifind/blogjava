package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZIP + Base64 工具类
 */
public final class GzipBase64Util {

    private GzipBase64Util() {
        // util class
    }

    /**
     * byte[] -> GZIP -> Base64
     */
    public static String compressToBase64(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return null;
        }

        try (
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut)
        ) {
            gzipOut.write(data);
            gzipOut.finish();
            return Base64.getEncoder().encodeToString(byteOut.toByteArray());
        }
    }

    /**
     * Base64 -> GZIP -> byte[]
     */
    public static byte[] decompressFromBase64(String base64) throws IOException {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }

        byte[] compressed = Base64.getDecoder().decode(base64);

        try (
                ByteArrayInputStream byteIn = new ByteArrayInputStream(compressed);
                GZIPInputStream gzipIn = new GZIPInputStream(byteIn);
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream()
        ) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                byteOut.write(buffer, 0, len);
            }
            return byteOut.toByteArray();
        }
    }
}