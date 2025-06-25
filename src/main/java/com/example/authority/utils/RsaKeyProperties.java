package com.example.authority.utils;


import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

@Component
public class RsaKeyProperties {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String publicKeyStr;

    public RsaKeyProperties() {
        try {
            KeyPair keyPair = RSAUtil.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            this.publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate RSA key pair", e);
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getPublicKeyStr() {
        return publicKeyStr;
    }
}