# ============================================
# Hospital Platform Dockerfile
# 多阶段构建：先编译，再运行
# ============================================

# 阶段1：构建
FROM gradle:7.6-jdk8 AS builder

WORKDIR /build

# 复制 Gradle 配置文件
COPY build.gradle settings.gradle gradle ./
COPY gradlew gradlew.bat ./

# 复制各模块配置
COPY hospital-common/build.gradle ./hospital-common/
COPY hospital-qmg/build.gradle ./hospital-qmg/
COPY hospital-neuroimmune/build.gradle ./hospital-neuroimmune/
COPY hospital-ocr/build.gradle ./hospital-ocr/
COPY hospital-web/build.gradle ./hospital-web/

# 下载依赖（利用缓存）
RUN gradle dependencies --no-daemon || return 0

# 复制源代码
COPY hospital-common/src ./hospital-common/src
COPY hospital-qmg/src ./hospital-qmg/src
COPY hospital-neuroimmune/src ./hospital-neuroimmune/src
COPY hospital-ocr/src ./hospital-ocr/src
COPY hospital-web/src ./hospital-web/src

# 构建 JAR
RUN gradle bootJar --no-daemon -x test

# 阶段2：运行
FROM openjdk:8-jre-slim

WORKDIR /app

# 复制构建产物
COPY --from=builder /build/hospital-web/build/libs/*.jar app.jar

# 创建非 root 用户
RUN groupadd -r hospital && useradd -r -g hospital hospital
USER hospital

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]