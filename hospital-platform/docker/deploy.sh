#!/bin/bash
# ============================================
# 医院平台快速部署脚本
# ============================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$SCRIPT_DIR"

echo "============================================"
echo "医院平台 Docker 部署"
echo "============================================"

# 检查 .env 文件
if [ ! -f ".env" ]; then
    echo "创建 .env 配置文件..."
    cp .env.example .env
    echo "请编辑 .env 文件配置密码等信息"
    echo "然后重新运行此脚本"
    exit 1
fi

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "错误: Docker Compose 未安装"
    exit 1
fi

echo ""
echo "部署选项:"
echo "1) 完整部署（所有服务）"
echo "2) 仅数据库（MySQL + Redis）"
echo "3) 仅后端服务"
echo "4) 仅前端服务"
echo "5) 停止所有服务"
echo "6) 查看服务状态"
echo "7) 查看日志"
echo "8) 清理（停止并删除数据）"
echo ""
read -p "请选择 [1-8]: " choice

case $choice in
    1)
        echo "启动所有服务..."
        docker-compose up -d
        echo ""
        echo "服务访问地址:"
        echo "  门户导航:      http://localhost/"
        echo "  QMG Admin:     http://localhost/qmg-admin/"
        echo "  Neuro Admin:   http://localhost/neuroimmune-admin/"
        echo "  超管平台:      http://localhost/super-admin/"
        echo "  API:           http://localhost/api/v1/"
        echo ""
        echo "小程序通过微信开发者工具部署"
        ;;
    2)
        echo "启动数据库服务..."
        docker-compose up -d mysql redis
        ;;
    3)
        echo "启动后端服务..."
        docker-compose up -d backend
        ;;
    4)
        echo "启动前端服务..."
        docker-compose up -d qmg-admin neuroimmune-admin portal
        ;;
    5)
        echo "停止所有服务..."
        docker-compose down
        ;;
    6)
        docker-compose ps
        ;;
    7)
        read -p "查看哪个服务日志? [backend/mysql/redis/qmg-admin/neuroimmune-admin/all]: " log_service
        if [ "$log_service" = "all" ]; then
            docker-compose logs -f
        else
            docker-compose logs -f "$log_service"
        fi
        ;;
    8)
        read -p "确认删除所有数据? [y/N]: " confirm
        if [ "$confirm" = "y" ]; then
            docker-compose down -v
            echo "所有数据已删除"
        else
            echo "取消操作"
        fi
        ;;
    *)
        echo "无效选择"
        exit 1
        ;;
esac

echo ""
echo "完成!"