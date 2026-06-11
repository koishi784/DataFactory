package com.datafactory.common.utils;

import com.datafactory.common.config.AesProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES 对称加密工具类
 *
 * 用于数据库连接密码的加密存储和解密还原。
 * 使用 AES-256/GCM/NoPadding 模式，提供认证加密（Authenticated Encryption）。
 * 每次加密生成随机 IV，保证相同明文每次密文不同。
 * 密钥从配置文件（application.yaml）中读取，需为 32 字节。
 */
@Component
@RequiredArgsConstructor
public class AesUtils {

    private final AesProperties aesProperties;

    /** AES/GCM 算法标识 */
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    /** GCM 模式推荐 IV 长度（12 字节） */
    private static final int GCM_IV_LENGTH = 12;

    /** GCM 认证标签长度（128 位） */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * 加密明文
     *
     * @param plainText 明文（如数据库连接密码）
     * @return Base64 编码的密文字符串（包含 IV，格式：IV + 密文）
     */
    public String encrypt(String plainText) {
        try {
            byte[] keyBytes = aesProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            // 生成随机 IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            // 执行加密
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // IV + 密文拼接后 Base64 编码
            byte[] encrypted = ByteBuffer.allocate(iv.length + cipherText.length)
                    .put(iv)
                    .put(cipherText)
                    .array();
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败", e);
        }
    }

    /**
     * 解密密文
     *
     * @param encryptedBase64 Base64 编码的密文字符串（包含 IV）
     * @return 明文字符串
     */
    public String decrypt(String encryptedBase64) {
        try {
            byte[] keyBytes = aesProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            // Base64 解码
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);

            // 分离 IV 和密文
            ByteBuffer buffer = ByteBuffer.wrap(encrypted);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            // 初始化解密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            // 执行解密
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES 解密失败", e);
        }
    }
}
