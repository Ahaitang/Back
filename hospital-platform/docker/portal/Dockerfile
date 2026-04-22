# ============================================
# 门户导航页面 Dockerfile
# 简单的静态 HTML 页面
# ============================================

FROM nginx:alpine

# 复制门户页面
COPY index.html /usr/share/nginx/html/index.html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=5s --retries=3 \
    CMD curl -f http://localhost/ || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]