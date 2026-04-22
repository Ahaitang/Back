# ============================================
# Manual Sync Script (Windows PowerShell)
# Use scp to sync code when rsync is not available
# ============================================

param(
    [string]$Command = "sync"
)

# Server configuration
$SERVER_HOST = if ($env:SERVER_HOST) { $env:SERVER_HOST } else { "101.201.30.29" }
$SERVER_USER = if ($env:SERVER_USER) { $env:SERVER_USER } else { "root" }
$SERVER_PORT = if ($env:SERVER_PORT) { $env:SERVER_PORT } else { "22" }
$SERVER_PATH = if ($env:SERVER_PATH) { $env:SERVER_PATH } else { "/opt/app/hospital-platform" }

# Local path (script is at: E:\CODE\BS\Back\hospital-platform\docker\sync.ps1)
# Need to go up 3 levels to reach project root E:\CODE\BS
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$BASE_DIR = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $SCRIPT_DIR))
$BACKEND_DIR = "$BASE_DIR\Back\hospital-platform"
$FRONTEND_DIR = "$BASE_DIR\Front"

# SSH target
$SSH_TARGET = "${SERVER_USER}@${SERVER_HOST}"

# ============================================
# Functions
# ============================================
function Write-Info { Write-Host "[INFO] $args" -ForegroundColor Green }
function Write-Warn { Write-Host "[WARN] $args" -ForegroundColor Yellow }
function Write-Err  { Write-Host "[ERROR] $args" -ForegroundColor Red }

function SSH-Exec {
    param([string]$Cmd)
    ssh -p $SERVER_PORT $SSH_TARGET $Cmd
}

function SCP-Dir {
    param([string]$Local, [string]$Remote)
    scp -P $SERVER_PORT -r $Local "${SSH_TARGET}:${Remote}"
}

function SCP-File {
    param([string]$Local, [string]$Remote)
    scp -P $SERVER_PORT $Local "${SSH_TARGET}:${Remote}"
}

# ============================================
# Create directories on server
# ============================================
function Create-Dirs {
    Write-Info "Creating directories on server..."
    SSH-Exec "mkdir -p $SERVER_PATH/Back/hospital-platform/docker"
    SSH-Exec "mkdir -p $SERVER_PATH/Back/hospital-platform/sql"
    SSH-Exec "mkdir -p $SERVER_PATH/Back/hospital-platform/hospital-common"
    SSH-Exec "mkdir -p $SERVER_PATH/Back/hospital-platform/hospital-qmg"
    SSH-Exec "mkdir -p $SERVER_PATH/Back/hospital-platform/hospital-neuroimmune"
    SSH-Exec "mkdir -p $SERVER_PATH/Back/hospital-platform/hospital-web"
    SSH-Exec "mkdir -p $SERVER_PATH/Back/hospital-platform/hospital-ocr"
    SSH-Exec "mkdir -p $SERVER_PATH/Back/hospital-platform/gradle"
    SSH-Exec "mkdir -p $SERVER_PATH/Front/qmg-admin"
    SSH-Exec "mkdir -p $SERVER_PATH/Front/neuroimmune-admin"
    Write-Info "Directories created"
}

# ============================================
# Clean - Remove all containers and volumes
# ============================================
function Clean-All {
    Write-Warn "This will remove all containers, images, and data volumes!"
    $confirm = Read-Host "Are you sure? [y/N]"
    if ($confirm -ne "y") {
        Write-Info "Cancelled"
        return
    }

    Write-Info "Stopping and removing all containers..."
    SSH-Exec "cd $SERVER_PATH/Back/hospital-platform/docker; docker-compose down -v"

    Write-Info "Removing orphan images..."
    SSH-Exec "docker image prune -f"

    Write-Info "Clean completed"
}

# ============================================
# Sync Docker configs
# ============================================
function Sync-Docker {
    Write-Info "Syncing docker configs..."
    SCP-Dir "$BACKEND_DIR\docker" "$SERVER_PATH/Back/hospital-platform/"
    Write-Info "Docker configs synced"
}

# ============================================
# Sync SQL scripts
# ============================================
function Sync-SQL {
    Write-Info "Syncing SQL scripts..."
    SCP-Dir "$BACKEND_DIR\sql" "$SERVER_PATH/Back/hospital-platform/"
    Write-Info "SQL scripts synced"
}

# ============================================
# Sync Backend modules
# ============================================
function Sync-Backend {
    Write-Info "Syncing backend modules..."

    Write-Info "  - hospital-common..."
    SCP-Dir "$BACKEND_DIR\hospital-common\src" "$SERVER_PATH/Back/hospital-platform/hospital-common/"

    Write-Info "  - hospital-qmg..."
    SCP-Dir "$BACKEND_DIR\hospital-qmg\src" "$SERVER_PATH/Back/hospital-platform/hospital-qmg/"

    Write-Info "  - hospital-neuroimmune..."
    SCP-Dir "$BACKEND_DIR\hospital-neuroimmune\src" "$SERVER_PATH/Back/hospital-platform/hospital-neuroimmune/"

    Write-Info "  - hospital-web..."
    SCP-Dir "$BACKEND_DIR\hospital-web\src" "$SERVER_PATH/Back/hospital-platform/hospital-web/"

    Write-Info "  - hospital-ocr..."
    SCP-Dir "$BACKEND_DIR\hospital-ocr\src" "$SERVER_PATH/Back/hospital-platform/hospital-ocr/"

    Write-Info "  - build.gradle & settings.gradle..."
    SCP-File "$BACKEND_DIR\build.gradle" "$SERVER_PATH/Back/hospital-platform/"
    SCP-File "$BACKEND_DIR\settings.gradle" "$SERVER_PATH/Back/hospital-platform/"

    Write-Info "  - gradle wrapper..."
    SCP-Dir "$BACKEND_DIR\gradle" "$SERVER_PATH/Back/hospital-platform/"
    SCP-File "$BACKEND_DIR\gradlew" "$SERVER_PATH/Back/hospital-platform/"
    SCP-File "$BACKEND_DIR\gradlew.bat" "$SERVER_PATH/Back/hospital-platform/"

    Write-Info "Backend synced"
}

# ============================================
# Sync Frontend Admin
# ============================================
function Sync-Frontend {
    Write-Info "Syncing frontend..."

    Write-Info "  - qmg-admin..."
    SCP-Dir "$FRONTEND_DIR\qmg-admin\src" "$SERVER_PATH/Front/qmg-admin/"
    SCP-File "$FRONTEND_DIR\qmg-admin\package.json" "$SERVER_PATH/Front/qmg-admin/"
    SCP-File "$FRONTEND_DIR\qmg-admin\vite.config.ts" "$SERVER_PATH/Front/qmg-admin/"
    SCP-File "$FRONTEND_DIR\qmg-admin\tsconfig.json" "$SERVER_PATH/Front/qmg-admin/"
    SCP-File "$FRONTEND_DIR\qmg-admin\index.html" "$SERVER_PATH/Front/qmg-admin/"

    Write-Info "  - neuroimmune-admin..."
    SCP-Dir "$FRONTEND_DIR\neuroimmune-admin\src" "$SERVER_PATH/Front/neuroimmune-admin/"
    SCP-File "$FRONTEND_DIR\neuroimmune-admin\package.json" "$SERVER_PATH/Front/neuroimmune-admin/"
    SCP-File "$FRONTEND_DIR\neuroimmune-admin\vite.config.ts" "$SERVER_PATH/Front/neuroimmune-admin/"
    SCP-File "$FRONTEND_DIR\neuroimmune-admin\tsconfig.json" "$SERVER_PATH/Front/neuroimmune-admin/"
    SCP-File "$FRONTEND_DIR\neuroimmune-admin\index.html" "$SERVER_PATH/Front/neuroimmune-admin/"

    Write-Info "Frontend synced"
}

# ============================================
# Full Sync
# ============================================
function Full-Sync {
    Write-Info "Starting full sync..."
    Create-Dirs
    Sync-Docker
    Sync-SQL
    Sync-Backend
    Sync-Frontend
    Write-Info "Full sync completed"
}

# ============================================
# Deploy (sync + start)
# ============================================
function Deploy {
    Full-Sync

    Write-Info "Starting Docker services..."
    SSH-Exec "cd $SERVER_PATH/Back/hospital-platform/docker; cp .env.example .env; docker-compose up -d --build"

    Write-Info "Deployment completed"
    Write-Host ""
    Write-Host "Access URLs:"
    Write-Host "  Portal:        http://${SERVER_HOST}/"
    Write-Host "  QMG Admin:     http://${SERVER_HOST}/qmg-admin/"
    Write-Host "  Neuro Admin:   http://${SERVER_HOST}/neuroimmune-admin/"
    Write-Host "  Super Admin:   http://${SERVER_HOST}/super-admin/"
}

# ============================================
# Rebuild - Clean + Sync + Build
# ============================================
function Rebuild-All {
    Write-Info "Starting full rebuild..."

    # Stop and clean
    Write-Info "Stopping existing containers..."
    SSH-Exec "cd $SERVER_PATH/Back/hospital-platform/docker; docker-compose down"

    # Remove old images
    Write-Info "Removing old images..."
    SSH-Exec "docker image prune -f"

    # Sync fresh code
    Full-Sync

    # Build and start
    Write-Info "Building and starting services..."
    SSH-Exec "cd $SERVER_PATH/Back/hospital-platform/docker; cp .env.example .env; docker-compose build --no-cache; docker-compose up -d"

    Write-Info "Rebuild completed"
    Write-Host ""
    Write-Host "Access URLs:"
    Write-Host "  Portal:        http://${SERVER_HOST}/"
    Write-Host "  QMG Admin:     http://${SERVER_HOST}/qmg-admin/"
    Write-Host "  Neuro Admin:   http://${SERVER_HOST}/neuroimmune-admin/"
    Write-Host "  Super Admin:   http://${SERVER_HOST}/super-admin/"
}

# ============================================
# View Status
# ============================================
function View-Status {
    SSH-Exec "cd $SERVER_PATH/Back/hospital-platform/docker; docker-compose ps"
}

# ============================================
# View Logs
# ============================================
function View-Logs {
    $service = Read-Host "Which service? [backend/mysql/redis/portal/all]"
    if ($service -eq "all") {
        SSH-Exec "cd $SERVER_PATH/Back/hospital-platform/docker; docker-compose logs --tail=100"
    } else {
        SSH-Exec "cd $SERVER_PATH/Back/hospital-platform/docker; docker-compose logs --tail=100 -f $service"
    }
}

# ============================================
# Show Help
# ============================================
function Show-Help {
    Write-Host "============================================"
    Write-Host "Hospital Platform Deploy Script"
    Write-Host "============================================"
    Write-Host ""
    Write-Host "Server: $SERVER_HOST"
    Write-Host "Path:   $SERVER_PATH"
    Write-Host ""
    Write-Host "Commands:"
    Write-Host "  sync       - Full sync (docker + sql + backend + frontend)"
    Write-Host "  docker     - Sync docker configs only"
    Write-Host "  sql        - Sync SQL scripts only"
    Write-Host "  backend    - Sync backend modules only"
    Write-Host "  frontend   - Sync frontend admin only"
    Write-Host "  deploy     - Full sync and start services"
    Write-Host "  rebuild    - Clean, sync, and rebuild all (no-cache)"
    Write-Host "  clean      - Remove all containers and volumes"
    Write-Host "  status     - View service status"
    Write-Host "  logs       - View service logs"
    Write-Host "  dirs       - Create directories on server"
    Write-Host "  help       - Show this help"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\sync.ps1 rebuild   # Full rebuild (recommended first deploy)"
    Write-Host "  .\sync.ps1 deploy    # Quick deploy"
    Write-Host "  .\sync.ps1 status    # Check status"
    Write-Host ""
}

# ============================================
# Main
# ============================================
switch ($Command) {
    "sync" { Full-Sync }
    "docker" { Sync-Docker }
    "sql" { Sync-SQL }
    "backend" { Sync-Backend }
    "frontend" { Sync-Frontend }
    "deploy" { Deploy }
    "rebuild" { Rebuild-All }
    "clean" { Clean-All }
    "status" { View-Status }
    "logs" { View-Logs }
    "dirs" { Create-Dirs }
    "help" { Show-Help }
    default { Write-Err "Unknown command: $Command"; Show-Help; exit 1 }
}