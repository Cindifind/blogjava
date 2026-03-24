package com.example.demo.auth.util;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class Argon2Util {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    public static final String SALT="hdiFwfFEQS164";

    public static String argon2Hash(String src, String salt) {
        try {
            byte[] password = src.getBytes(StandardCharsets.UTF_8);
            byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

            Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                    .withSalt(saltBytes)
                    .withIterations(3)
                    .withMemoryAsKB(65536)  // 64MB
                    .withParallelism(1)
                    .build();

            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(params);

            byte[] hash = new byte[32];  // 32字节 = 256位
            generator.generateBytes(password, hash);

            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static String generateRandomSalt(int length) {
        byte[] saltBytes = new byte[length];
        SECURE_RANDOM.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /**
     * 生成默认长度的随机盐值（16 字节）
     * @return Base64 编码的盐值字符串
     */
    public static String generateRandomSalt() {
        return generateRandomSalt(16);
    }

}
