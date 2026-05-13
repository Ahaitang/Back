package org.hospital.web.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 生产环境启动校验
 * 确保所有必需的环境变量已配置，防止带默认凭据启动
 */
@Component
@Profile("prod")
public class ProdEnvironmentValidator implements ApplicationRunner {

    private static final Logger log = LogManager.getLogger(ProdEnvironmentValidator.class);

    private static final List<String> REQUIRED_ENV_VARS = List.of(
        "JWT_SECRET",
        "SUPER_ADMIN_PASSWORD",
        "DB_QMG_URL",
        "DB_QMG_USER",
        "DB_QMG_PASSWORD",
        "DB_NEURO_URL",
        "DB_NEURO_USER",
        "DB_NEURO_PASSWORD",
        "MINIO_ENDPOINT",
        "MINIO_ACCESS_KEY",
        "MINIO_SECRET_KEY",
        "CORS_ALLOWED_ORIGINS"
    );

    @Override
    public void run(ApplicationArguments args) {
        List<String> missing = new ArrayList<>();

        for (String envVar : REQUIRED_ENV_VARS) {
            String value = System.getenv(envVar);
            if (value == null || value.isBlank()) {
                missing.add(envVar);
            }
        }

        if (!missing.isEmpty()) {
            log.error("========================================");
            log.error("FATAL: 生产环境缺少必需的环境变量:");
            for (String var : missing) {
                log.error("  - {}", var);
            }
            log.error("请配置以上环境变量后重新启动");
            log.error("参考: deploy/.env.example");
            log.error("========================================");
            System.exit(1);
        }

        log.info("Production environment validation passed - all required variables configured");
    }
}
