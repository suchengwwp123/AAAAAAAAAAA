package com.example.authority.utils;

import javax.crypto.Cipher;
import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 密钥工具类
 * @author authority
 */
public class RSAUtil {

    /**
     * 配置密钥对
     * @return
     * @throws Exception
     */
    public static KeyPair generateKeyPair() throws Exception {
        // 读取公钥
        String publicKeyStr = new String(
                new org.springframework.core.io.ClassPathResource("rsa/public.key").getInputStream().readAllBytes()
        );
        PublicKey publicKey = getPublicKey(publicKeyStr);

        // 读取私钥
        String privateKeyStr = new String(
                new org.springframework.core.io.ClassPathResource("rsa/private.key").getInputStream().readAllBytes()
        );
        PrivateKey privateKey = getPrivateKey(privateKeyStr);

        return new KeyPair(publicKey, privateKey);
    }


    // 公钥加密
    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 私钥解密
    public static String decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    // 从字符串加载公钥
    public static PublicKey getPublicKey(String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    // 从字符串加载私钥
    public static PrivateKey getPrivateKey(String base64PrivateKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    /**
     * 更新公钥和密钥
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 生成密钥对
        KeyPair keyPair = RSAUtil.generateKeyPair();

        // 指定 resources/rsa 路径（如果不存在就创建）
        File rsaDir = new File("src/main/resources/rsa");
        if (!rsaDir.exists()) {
            rsaDir.mkdirs();
        }

        // 保存公钥
        String publicKeyStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        Files.writeString(new File(rsaDir, "public.key").toPath(), publicKeyStr);

        // 保存私钥
        String privateKeyStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        Files.writeString(new File(rsaDir, "private.key").toPath(), privateKeyStr);

        System.out.println("RSA 密钥文件生成完成！");
        System.out.println("公钥: " + new File(rsaDir, "public.key").getAbsolutePath());
        System.out.println("私钥: " + new File(rsaDir, "private.key").getAbsolutePath());
    }
}