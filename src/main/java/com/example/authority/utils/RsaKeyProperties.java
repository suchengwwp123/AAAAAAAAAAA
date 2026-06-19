package com.example.authority.utils;


import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class RsaKeyProperties {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String publicKeyStr;

    @PostConstruct
    public void init() {
        try {
            // 读取公钥
            String publicKeyContent = Files.readString(
                    new ClassPathResource("rsa/public.key").getFile().toPath()
            );
            publicKey = RSAUtil.getPublicKey(publicKeyContent);
            publicKeyStr = publicKeyContent; // 直接用 Base64 字符串

            // 读取私钥
            String privateKeyContent = Files.readString(
                    new ClassPathResource("rsa/private.key").getFile().toPath()
            );
            privateKey = RSAUtil.getPrivateKey(privateKeyContent);

            log.info("RSA 密钥对已从文件加载成功！");
        } catch (Exception e) {
            throw new IllegalStateException("无法加载 RSA 密钥文件", e);
        }
    }


}
