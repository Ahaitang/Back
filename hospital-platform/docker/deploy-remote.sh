# ============================================
# 医院平台远程部署脚本 (Windows -> Linux)
# ============================================
# 使用方法:
# 1. 配置服务器信息: 编辑 deploy-remote.sh 或设置环境变量
# 2. 运行脚本: ./deploy-remote.sh [命令]
#
# 常用命令:
#   deploy     - 同步代码并启动服务
#   sync       - 仅同步代码
#   start      - 启动服务
#   stop       - 停止服务
#   logs       - 查看日志
#   status     - 查看状态
#   ssh        - SSH 连接服务器
# ============================================

#!/bin/bash

set -e

# ============================================
# 服务器配置（可修改或通过环境变量覆盖）
# ============================================
SERVER_HOST="${SERVER_HOST:-101.201.30.29}"       # 服务器 IP 或域名
SERVER_USER="${SERVER_USER:-root}"                 # SSH 用户名
SERVER_PORT="${SERVER_PORT: 22}"                   # SSH 端口
SERVER_PATH="${SERVER_PATH:-/opt/hospital-platform}"  # 服务器部署路径

# 本地路径
LOCAL_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
DOCKER_DIR="$LOCAL_DIR/Back/hospital-platform/docker"

# ============================================
# 颜色输出
# ============================================
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
echo_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
echo_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ============================================
# SSH 命令封装
# ============================================
ssh_cmd() {
    ssh -p $SERVER_PORT $SERVER_USER@$SERVER_HOST "$1"
}

scp_upload() {
    scp -P $SERVER_PORT "$1" "$SERVER_USER@$SERVER_HOST:$2"
}

# ============================================
# 检查配置
# ============================================
check_config() {
    if [ "$SERVER_HOST" = "your-server-ip" ]; then
        echo_error "请先配置服务器信息！"
        echo ""
        echo "方式一: 设置环境变量"
        echo "  export SERVER_HOST=101.201.30.29"
        echo "  export SERVER_USER=root"
        echo "  export SERVER_PATH=/opt/hospital-platform"
        echo ""
        echo "方式二: 直接修改脚本中的配置"
        echo ""
        echo "当前配置:"
        echo "  SERVER_HOST: $SERVER_HOST"
        echo "  SERVER_USER: $SERVER_USER"
        echo "  SERVER_PORT: $SERVER_PORT"
        echo "  SERVER_PATH: $SERVER_PATH"
        exit 1
    fi

    # 检查 SSH 连接
    echo_info "检查 SSH 连接..."
    if ! ssh_cmd "echo 'SSH OK'" 2>/dev/null; then
        echo_error "无法连接到服务器 $SERVER_HOST"
        echo "请检查:"
        echo "  1. 服务器 IP 是否正确"
        echo "  2. SSH 密钥是否配置 (ssh-copy-id $SERVER_USER@$SERVER_HOST)"
        echo "  3. 服务器是否允许 SSH 连接"
        exit 1
    fi
    echo_info "SSH 连接正常"
}

# ============================================
# 同步代码到服务器
# ============================================
sync_code() {
    echo_info "同步代码到服务器..."

    # 创建服务器目录
    ssh_cmd "mkdir -p $SERVER_PATH/{Back,Front,sql}"

    # 同步后端代码
    echo_info "同步后端代码..."
    rsync -avz --progress --delete \
        --exclude '.git' \
        --exclude 'build' \
        --exclude '.gradle' \
        --exclude '*.log' \
        --exclude '.env' \
        "$LOCAL_DIR/Back/hospital-platform/" \
        "$SERVER_USER@$SERVER_HOST:$SERVER_PATH/Back/hospital-platform/"

    # 同步前端 Admin 代码
    echo_info "同步前端 Admin 代码..."
    rsync -avz --progress --delete \
        --exclude '.git' \
        --exclude 'node_modules' \
        --exclude 'dist' \
        --exclude '*.log' \
        "$LOCAL_DIR/Front/qmg-admin/" \
        "$SERVER_USER@$SERVER_HOST:$SERVER_PATH/Front/qmg-admin/"

    rsync -avz --progress --delete \
        --exclude '.git' \
        --exclude 'node_modules' \
        --exclude 'dist' \
        --exclude '*.log' \
        "$LOCAL_DIR/Front/neuroimmune-admin/" \
        "$SERVER_USER@$SERVER_HOST:$SERVER_PATH/Front/neuroimmune-admin/"

    # 同步 SQL 文件
    echo_info "同步 SQL 初始化脚本..."
    rsync -avz --progress \
        "$LOCAL_DIR/Back/hospital-platform/sql/" \
        "$SERVER_USER@$SERVER_HOST:$SERVER_PATH/sql/"

    echo_info "代码同步完成"
}

# ============================================
# 部署服务
# ============================================
deploy() {
    echo_info "开始部署..."

    # 同步代码
    sync_code

    # 在服务器上执行部署
    echo_info "在服务器上启动 Docker 服务..."
    ssh_cmd "cd $SERVER_PATH/Back/hospital-platform/docker && \
        if [ ! -f .env ]; then cp .env.example .env; fi && \
        docker-compose up -d --build"

    echo_info "部署完成"
    echo ""
    echo "访问地址:"
    echo "  门户导航:      http://$SERVER_HOST/"
    echo "  QMG Admin:     http://$SERVER_HOST/qmg-admin/"
    echo "  Neuro Admin:   http://$SERVER_HOST/neuroimmune-admin/"
    echo "  超管平台:      http://$SERVER_HOST/super-admin/"
}

# ============================================
# 启动服务
# ============================================
start_services() {
    echo_info "启动服务..."
    ssh_cmd "cd $SERVER_PATH/Back/hospital-platform/docker && docker-compose up -d"
    echo_info "服务已启动"
}

# ============================================
# 停止服务
# ============================================
stop_services() {
    echo_info "停止服务..."
    ssh_cmd "cd $SERVER_PATH/Back/hospital-platform/docker && docker-compose down"
    echo_info "服务已停止"
}

# ============================================
# 查看状态
# ============================================
view_status() {
    ssh_cmd "cd $SERVER_PATH/Back/hospital-platform/docker && docker-compose ps"
}

# ============================================
# 查看日志
# ============================================
view_logs() {
    read -p "查看哪个服务日志? [backend/mysql/redis/portal/all]: " service
    if [ "$service" = "all" ]; then
        ssh_cmd "cd $SERVER_PATH/Back/hospital-platform/docker && docker-compose logs --tail=100"
    else
        ssh_cmd "cd $SERVER_PATH/Back/hospital-platform/docker && docker-compose logs --tail=100 -f $service"
    fi
}

# ============================================
# 重建服务
# ============================================
rebuild() {
    echo_info "重新构建服务..."
    ssh_cmd "cd $SERVER_PATH/Back/hospital-platform/docker && docker-compose build --no-cache && docker-compose up -d"
    echo_info "重建完成"
}

# ============================================
# 清理服务
# ============================================
cleanup() {
    read -p "确认删除所有数据? [y/N]: " confirm
    if [ "$confirm" = "y" ]; then
        echo_warn "删除所有服务和数据..."
        ssh_cmd "cd $SERVER_PATH/Back/hospital-platform/docker && docker-compose down -v"
        echo_info "清理完成"
    else
        echo_info "取消操作"
    fi
}

# ============================================
# SSH 连接
# ============================================
connect_ssh() {
    echo_info "连接到服务器..."
    ssh -p $SERVER_PORT $SERVER_USER@$SERVER_HOST
}

# ============================================
# 初始化服务器
# ============================================
init_server() {
    echo_info "初始化服务器环境..."

    ssh_cmd << 'EOF'
        # 检查 Docker
        if ! command -v docker &> /dev/null; then
            echo "安装 Docker..."
            curl -fsSL https://get.docker.com | sh
            systemctl enable docker
            systemctl start docker
        fi

        # 检查 Docker Compose
        if ! command -v docker-compose &> /dev/null; then
            echo "安装 Docker Compose..."
            curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
            chmod +x /usr/local/bin/docker-compose
        fi

        echo "Docker 版本:"
        docker --version
        docker-compose --version

        # 创建部署目录
        mkdir -p /opt/hospital-platform
    EOF

    echo_info "服务器环境初始化完成"
}

# ============================================
# 主菜单
# ============================================
show_help() {
    echo "============================================"
    echo "医院平台远程部署脚本"
    echo "============================================"
    echo ""
    echo "当前服务器: $SERVER_HOST"
    echo "部署路径:   $SERVER_PATH"
    echo ""
    echo "命令:"
    echo "  deploy   - 同步代码并部署服务"
    echo "  sync     - 仅同步代码到服务器"
    echo "  start    - 启动服务"
    echo "  stop     - 停止服务"
    echo "  restart  - 重启服务"
    echo "  status   - 查看服务状态"
    echo "  logs     - 查看服务日志"
    echo "  rebuild  - 重新构建服务"
    echo "  cleanup  - 清理服务和数据"
    echo "  init     - 初始化服务器环境"
    echo "  ssh      - SSH 连接服务器"
    echo "  help     - 显示帮助"
    echo ""
    echo "环境变量配置:"
    echo "  SERVER_HOST    - 服务器 IP (默认: your-server-ip)"
    echo "  SERVER_USER    - SSH 用户 (默认: root)"
    echo "  SERVER_PORT    - SSH 端口 (默认: 22)"
    echo "  SERVER_PATH    - 部署路径 (默认: /opt/hospital-platform)"
    echo ""
}

# ============================================
# 主程序
# ============================================
case "${1:-help}" in
    deploy)
        check_config
        deploy
        ;;
    sync)
        check_config
        sync_code
        ;;
    start)
        check_config
        start_services
        ;;
    stop)
        check_config
        stop_services
        ;;
    restart)
        check_config
        stop_services
        start_services
        ;;
    status)
        check_config
        view_status
        ;;
    logs)
        check_config
        view_logs
        ;;
    rebuild)
        check_config
        rebuild
        ;;
    cleanup)
        check_config
        cleanup
        ;;
    init)
        check_config
        init_server
        ;;
    ssh)
        check_config
        connect_ssh
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo_error "未知命令: $1"
        show_help
        exit 1
        ;;
esac