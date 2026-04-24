package org.hospital.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API 文档配置 (Spring Boot 3.x)
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI hospitalPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hospital Platform API")
                        .description("医院平台统一 API 文档<br/>包含 QMG（重症肌无力评分系统）和 Neuroimmune（神经免疫疾病随访系统）")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hospital Platform Team")
                                .email("support@hospital.org"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}