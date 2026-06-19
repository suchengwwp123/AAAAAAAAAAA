package com.example.authority.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AliPayConfig {
    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    @Value("{alipay.appId}")
    public String appId;

    // 应用私钥，就是工具生成的应用私钥
    @Value("{alipay.merchantPrivateKey}")
    public String merchantPrivateKey;
    // 支付宝公钥,对应APPID下的支付宝公钥。
    @Value("{alipay.alipayPublicKey}")
    public String alipayPublicKey;
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    @Value("{alipay.notifyUrl}")
    public String notifyUrl;




}
