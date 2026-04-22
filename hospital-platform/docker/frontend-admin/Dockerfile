# ============================================
# 前端 Admin Dockerfile
# Vue 3 + Vite 多阶段构建
# 用于 qmg-admin 和 neuroimmune-admin
# ============================================

# 构建阶段
FROM node:20-alpine AS builder

WORKDIR /build

# 复制依赖文件
COPY package.json package-lock.json* ./

# 安装依赖
RUN npm ci --legacy-peer-deps || npm install --legacy-peer-deps

# 复制源代码
COPY . .

# 构建生产版本 (skip TypeScript check for production)
RUN npx vite build

# 运行阶段 - 使用 nginx
FROM nginx:alpine

# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost/ || exit 1

# 暴露端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]