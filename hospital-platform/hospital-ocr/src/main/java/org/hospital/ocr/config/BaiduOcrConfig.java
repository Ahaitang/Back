package org.hospital.ocr.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 百度OCR配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "baidu.ocr")
public class BaiduOcrConfig {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * Secret Key
     */
    private String secretKey;

    /**
     * 连接超时时间(毫秒)
     */
    private Integer connectTimeout = 30000;

    /**
     * 读取超时时间(毫秒)
     */
    private Integer readTimeout = 30000;
}