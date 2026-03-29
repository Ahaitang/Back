package org.hospital.neuroimmune.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * OCR API 配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ocr.api")
public class OcrApiConfig {

    private String baseUrl;
    private Integer timeout = 60000;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}