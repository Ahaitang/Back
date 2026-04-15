package org.hospital.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QMG 模块 API 分组配置
 */
@Configuration
public class QmgOpenApiConfig {

    @Bean
    public GroupedOpenApi qmgGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("QMG - 重症肌无力评分系统")
                .pathsToMatch("/api/v1/qmg/**")
                .addOpenApiMethodFilter(method -> {
                    String className = method.getDeclaringClass().getPackage().getName();
                    return className.contains("qmg");
                })
                .build();
    }
}