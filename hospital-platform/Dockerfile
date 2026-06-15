# ============================================================
# Hospital Platform — 直接打包预构建 JAR
# 在宿主机用 ./gradlew :hospital-web:bootJar 构建后再构建镜像
# ============================================================

FROM eclipse-temurin:17-jre

LABEL maintainer="hospital-platform"
LABEL description="Hospital Platform Backend Service"

# 创建非 root 用户
RUN groupadd -r hospital && useradd -r -g hospital -d /app hospital

WORKDIR /app

# 复制预构建的 JAR（在宿主机编译后上传）
COPY hospital-web/build/libs/hospital-platform.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R hospital:hospital /app

# 切换到非 root 用户
USER hospital

# 暴露端口
EXPOSE 8080

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -sf http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]
