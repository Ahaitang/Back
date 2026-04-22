# ============================================
# 后端服务 Dockerfile
# Spring Boot 多阶段构建
# ============================================

# 构建阶段
FROM gradle:7.6-jdk8 AS builder

WORKDIR /build

# 复制 Gradle 配置文件
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 复制子项目配置
COPY hospital-common/build.gradle ./hospital-common/
COPY hospital-qmg/build.gradle ./hospital-qmg/
COPY hospital-neuroimmune/build.gradle ./hospital-neuroimmune/
COPY hospital-web/build.gradle ./hospital-web/
COPY hospital-ocr/build.gradle ./hospital-ocr/

# 复制源代码
COPY hospital-common/src ./hospital-common/src
COPY hospital-qmg/src ./hospital-qmg/src
COPY hospital-neuroimmune/src ./hospital-neuroimmune/src
COPY hospital-web/src ./hospital-web/src
COPY hospital-ocr/src ./hospital-ocr/src

# 构建 JAR（跳过测试）
RUN gradle clean bootJar -x test --no-daemon

# 运行阶段
FROM openjdk:8-jre-slim

WORKDIR /app

# 安装必要工具
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 从构建阶段复制 JAR
COPY --from=builder /build/hospital-web/build/libs/*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]