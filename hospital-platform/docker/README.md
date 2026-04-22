# 医院平台 Docker 部署指南

## 目录结构

```
docker/
├── backend/
│   └── Dockerfile          # 后端 Spring Boot 构建
├── frontend-admin/
│   ├── Dockerfile          # Admin Web (Vue 3) 构建
│   └── nginx.conf          # Admin nginx 配置
├── portal/
│   ├── Dockerfile          # 门户导航页构建
│   ├── nginx.conf          # Portal nginx 配置（含路由转发）
│   └── index.html          # 统一入口导航页面
├── nginx/
│   └── nginx.conf          # Gateway nginx 配置（可选）
├── docker-compose.yml      # 主部署文件（含门户）
├── docker-compose.gateway.yml  # Gateway 部署（可选）
└── .env.example            # 环境变量示例
```

**注意：小程序通过微信开发者工具部署，不在 Docker 中。**

## 统一入口路由

门户页面作为统一入口，通过路由前缀转发到不同子系统：

| 路由 | 目标 | 说明 |
|------|------|------|
| `/` | Portal | 门户导航页面 |
| `/qmg-admin/` | QMG Admin | 重症肌无力评分管理后台 |
| `/neuroimmune-admin/` | Neuroimmune Admin | 神经免疫随访管理后台 |
| `/super-admin/` | Backend | 超级管理员平台 |
| `/api/v1/` | Backend API | 后端 REST API |

## 快速部署

### 1. 准备配置文件

```bash
# 复制环境变量配置
cp .env.example .env

# 编辑配置（修改密码等）
vim .env
```

### 2. 启动所有服务

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

### 3. 服务访问地址

**统一入口：**
- **门户导航页面**：http://localhost/
  - 点击卡片进入各子系统
  - 显示各系统运行状态

**子系统路由：**
- QMG Admin：http://localhost/qmg-admin/
- Neuroimmune Admin：http://localhost/neuroimmune-admin/
- 超管平台：http://localhost/super-admin/
- API：http://localhost/api/v1/

**独立访问（调试用）：**
- 后端 API：http://localhost:8080/api/v1

**小程序部署：** 使用微信开发者工具打开 `Front/qmg-app` 和 `Front/neuroimmune-app`，配置服务器域名后上传发布。

## 单独构建/启动服务

### 仅启动数据库和缓存

```bash
docker-compose up -d mysql redis
```

### 仅启动后端

```bash
docker-compose up -d backend
```

### 仅启动前端和门户

```bash
docker-compose up -d qmg-admin neuroimmune-admin portal
```

## 生产环境部署

### 1. 修改配置

编辑 `.env` 文件：

```bash
# 使用强密码
MYSQL_ROOT_PASSWORD=your-strong-password
REDIS_PASSWORD=your-strong-password
JWT_SECRET=your-jwt-secret-key

# 关闭 Swagger
SWAGGER_ENABLED=false

# 配置 MinIO（如有）
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
```

### 2. 配置 HTTPS（推荐）

1. 准备 SSL 证书：
```bash
mkdir -p portal/ssl
# 将证书文件放入 portal/ssl/ 目录
# your-domain.com.crt
# your-domain.com.key
```

2. 修改 `portal/nginx.conf`，添加 HTTPS 配置：

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/nginx/ssl/your-domain.com.crt;
    ssl_certificate_key /etc/nginx/ssl/your-domain.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    
    # ... 其他 location 配置同上
}
```

3. 修改 `docker-compose.yml`，添加 SSL 卷挂载和端口：

```yaml
portal:
  volumes:
    - ./portal/ssl:/etc/nginx/ssl:ro
  ports:
    - "80:80"
    - "443:443"
```

4. 重启服务：
```bash
docker-compose up -d --force-recreate portal
```

## 常用命令

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（清除所有数据）
docker-compose down -v

# 重新构建镜像
docker-compose build --no-cache

# 查看容器日志
docker-compose logs -f [service-name]

# 进入容器
docker exec -it hospital-backend sh

# 查看 MySQL 数据
docker exec -it hospital-mysql mysql -uroot -p

# 查看 Redis 数据
docker exec -it hospital-redis redis-cli -a your-password
```

## 数据备份

### MySQL 备份

```bash
# 备份所有数据库
docker exec hospital-mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} \
  --databases QMG neuroimmune session_audit \
  > backup_$(date +%Y%m%d).sql

# 恢复数据
docker exec -i hospital-mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} < backup.sql
```

### Redis 备份

```bash
# Redis 自动持久化到 appendonly.aof
docker exec hospital-redis redis-cli -a ${REDIS_PASSWORD} BGSAVE
```

## 故障排查

### 后端无法连接数据库

1. 等待 MySQL 完全启动（约 60 秒）
2. 检查 MySQL 健康：
```bash
docker-compose ps mysql
```
3. 查看后端日志：
```bash
docker-compose logs backend
```

### 门户页面无法访问子系统

检查 portal/nginx.conf 中的代理配置是否正确指向各服务容器名。

### 内存不足

调整 `.env` 中的 JAVA_OPTS：
```bash
JAVA_OPTS=-Xms256m -Xmx512m
```

## 服务器要求

- Docker 20.10+
- Docker Compose 2.0+
- 内存: 最低 2GB，推荐 4GB+
- 存储: 最低 10GB（数据库数据）

## 远程部署（Windows 开发 -> Linux 服务器）

### 1. 配置服务器信息

编辑 `deploy-remote.ps1` 或设置环境变量：

```powershell
# PowerShell
$env:SERVER_HOST = "101.201.30.29"
$env:SERVER_USER = "root"
$env:SERVER_PATH = "/opt/hospital-platform"
```

或直接修改脚本中的默认值。

### 2. 确保 SSH 可用

Windows 10/11 已内置 OpenSSH，检查：

```powershell
# 检查 SSH
ssh -V

# 测试连接
ssh root@101.201.30.29

# 如需配置密钥（避免每次输入密码）
ssh-keygen -t rsa
ssh-copy-id root@101.201.30.29
```

### 3. 远程部署命令

```powershell
# 进入 docker 目录
cd E:\CODE\BS\Back\hospital-platform\docker

# 部署（同步代码 + 启动服务）
.\deploy-remote.ps1 deploy

# 或使用 bat 文件
deploy-remote.bat deploy
```

### 4. 其他远程命令

| 命令 | 说明 |
|------|------|
| `deploy` | 同步代码并部署 |
| `sync` | 仅同步代码 |
| `start` | 启动服务 |
| `stop` | 停止服务 |
| `status` | 查看状态 |
| `logs` | 查看日志 |
| `rebuild` | 重新构建 |
| `init` | 初始化服务器（安装 Docker） |
| `ssh` | SSH 连接服务器 |

### 5. 安装 rsync（推荐）

rsync 同步更快，支持增量传输：

```powershell
# 方式一: 使用 WSL (推荐)
wsl --install

# 方式二: 安装 cwRsync
# 下载: https://www.itefix.net/cwrsync
```

### 6. 服务器首次部署

```powershell
# 1. 初始化服务器环境（安装 Docker）
.\deploy-remote.ps1 init

# 2. 部署服务
.\deploy-remote.ps1 deploy

# 3. 查看状态
.\deploy-remote.ps1 status
```

部署完成后访问: http://101.201.30.29/