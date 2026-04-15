package org.hospital.web.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Neuroimmune 模块 API 分组配置
 */
@Configuration
public class NeuroimmuneOpenApiConfig {

    @Bean
    public GroupedOpenApi neuroimmuneGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("Neuroimmune - 神经免疫疾病随访系统")
                .pathsToMatch("/api/v1/neuroimmune/**")
                .addOpenApiMethodFilter(method -> {
                    String className = method.getDeclaringClass().getPackage().getName();
                    return className.contains("neuroimmune");
                })
                .build();
    }
}