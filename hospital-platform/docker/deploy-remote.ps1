# ============================================
# Hospital Platform Remote Deploy Script (Windows PowerShell)
# ============================================
# Usage:
# 1. Edit server config or set environment variables
# 2. Run: .\deploy-remote.ps1 [command]
#
# Commands: deploy, sync, start, stop, logs, status, ssh, init
# ============================================

param(
    [string]$Command = "help"
)

# Set UTF-8 encoding for output
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# ============================================
# Server Configuration
# ============================================
$SERVER_HOST = if ($env:SERVER_HOST) { $env:SERVER_HOST } else { "101.201.30.29" }
$SERVER_USER = if ($env:SERVER_USER) { $env:SERVER_USER } else { "root" }
$SERVER_PORT = if ($env:SERVER_PORT) { $env:SERVER_PORT } else { "22" }
$SERVER_PATH = if ($env:SERVER_PATH) { $env:SERVER_PATH } else { "/opt/hospital-platform" }

# Local path
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$LOCAL_DIR = Split-Path -Parent (Split-Path -Parent $SCRIPT_DIR)

# SSH target
$SSH_TARGET = "${SERVER_USER}@${SERVER_HOST}"

# ============================================
# Color Output
# ============================================
function Write-Info { Write-Host "[INFO] $args" -ForegroundColor Green }
function Write-Warn { Write-Host "[WARN] $args" -ForegroundColor Yellow }
function Write-Err  { Write-Host "[ERROR] $args" -ForegroundColor Red }

# ============================================
# SSH Functions
# ============================================
function SSH-Exec {
    param([string]$Cmd)
    ssh -p $SERVER_PORT $SSH_TARGET $Cmd
}

function SCP-Upload {
    param([string]$Local, [string]$Remote)
    $remotePath = "${SSH_TARGET}:${Remote}"
    scp -P $SERVER_PORT $Local $remotePath
}

# ============================================
# Check SSH Connection
# ============================================
function Check-Connection {
    Write-Info "Checking SSH connection..."
    $result = ssh -p $SERVER_PORT $SSH_TARGET "echo OK" 2>&1
    if ($result -ne "OK") {
        Write-Err "Cannot connect to server: $SERVER_HOST"
        Write-Host "Please check:"
        Write-Host "  1. Server IP is correct"
        Write-Host "  2. SSH is configured (try: ssh $SSH_TARGET)"
        Write-Host "  3. OpenSSH is installed on Windows"
        Write-Host ""
        Write-Host "If password required, run: ssh $SSH_TARGET first"
        exit 1
    }
    Write-Info "SSH connection OK"
}

# ============================================
# Sync Code
# ============================================
function Sync-Code {
    Write-Info "Syncing code to server..."

    # Create server directories
    SSH-Exec "mkdir -p $SERVER_PATH/{Back,Front,sql}"

    # Check rsync
    $rsyncAvailable = Get-Command rsync -ErrorAction SilentlyContinue
    if ($rsyncAvailable) {
        Write-Info "Using rsync..."

        # Sync backend
        Write-Info "Syncing backend..."
        $backendRemote = "${SSH_TARGET}:${SERVER_PATH}/Back/hospital-platform/"
        rsync -avz --delete --exclude '.git' --exclude 'build' --exclude '.gradle' --exclude '*.log' --exclude '.env' `
            "$LOCAL_DIR/Back/hospital-platform/" $backendRemote

        # Sync frontend
        Write-Info "Syncing frontend..."
        $qmgRemote = "${SSH_TARGET}:${SERVER_PATH}/Front/qmg-admin/"
        rsync -avz --delete --exclude '.git' --exclude 'node_modules' --exclude 'dist' --exclude '*.log' `
            "$LOCAL_DIR/Front/qmg-admin/" $qmgRemote

        $neuroRemote = "${SSH_TARGET}:${SERVER_PATH}/Front/neuroimmune-admin/"
        rsync -avz --delete --exclude '.git' --exclude 'node_modules' --exclude 'dist' --exclude '*.log' `
            "$LOCAL_DIR/Front/neuroimmune-admin/" $neuroRemote
    } else {
        Write-Warn "rsync not available. Please install WSL or use manual sync."
        Write-Info "Hint: wsl --install, then run deploy-remote.sh in WSL"
        return
    }

    # Sync SQL
    Write-Info "Syncing SQL scripts..."
    $sqlRemote = "${SSH_TARGET}:${SERVER_PATH}/sql/"
    scp -P $SERVER_PORT -r "$LOCAL_DIR/Back/hospital-platform/sql/*" $sqlRemote

    Write-Info "Code sync completed"
}

# ============================================
# Deploy
# ============================================
function Deploy {
    Write-Info "Starting deployment..."

    Sync-Code

    Write-Info "Starting Docker services on server..."
    $deployCmd = "cd ${SERVER_PATH}/Back/hospital-platform/docker; if [ ! -f .env ]; then cp .env.example .env; fi; docker-compose up -d --build"
    SSH-Exec $deployCmd

    Write-Info "Deployment completed"
    Write-Host ""
    Write-Host "Access URLs:"
    Write-Host "  Portal:        http://${SERVER_HOST}/"
    Write-Host "  QMG Admin:     http://${SERVER_HOST}/qmg-admin/"
    Write-Host "  Neuro Admin:   http://${SERVER_HOST}/neuroimmune-admin/"
    Write-Host "  Super Admin:   http://${SERVER_HOST}/super-admin/"
}

# ============================================
# Start/Stop Services
# ============================================
function Start-Services {
    Write-Info "Starting services..."
    SSH-Exec "cd ${SERVER_PATH}/Back/hospital-platform/docker; docker-compose up -d"
    Write-Info "Services started"
}

function Stop-Services {
    Write-Info "Stopping services..."
    SSH-Exec "cd ${SERVER_PATH}/Back/hospital-platform/docker; docker-compose down"
    Write-Info "Services stopped"
}

# ============================================
# View Status/Logs
# ============================================
function View-Status {
    SSH-Exec "cd ${SERVER_PATH}/Back/hospital-platform/docker; docker-compose ps"
}

function View-Logs {
    $service = Read-Host "Which service? [backend/mysql/redis/portal/all]"
    if ($service -eq "all") {
        SSH-Exec "cd ${SERVER_PATH}/Back/hospital-platform/docker; docker-compose logs --tail=100"
    } else {
        SSH-Exec "cd ${SERVER_PATH}/Back/hospital-platform/docker; docker-compose logs --tail=100 -f $service"
    }
}

# ============================================
# Rebuild
# ============================================
function Rebuild {
    Write-Info "Rebuilding services..."
    SSH-Exec "cd ${SERVER_PATH}/Back/hospital-platform/docker; docker-compose build --no-cache; docker-compose up -d"
    Write-Info "Rebuild completed"
}

# ============================================
# Init Server
# ============================================
function Init-Server {
    Write-Info "Initializing server environment..."

    SSH-Exec "bash -c 'if ! command -v docker &> /dev/null; then curl -fsSL https://get.docker.com | sh; systemctl enable docker; systemctl start docker; fi'"
    SSH-Exec "bash -c 'if ! command -v docker-compose &> /dev/null; then curl -L https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose; chmod +x /usr/local/bin/docker-compose; fi'"
    SSH-Exec "docker --version; docker-compose --version; mkdir -p $SERVER_PATH"

    Write-Info "Server initialized"
}

# ============================================
# SSH Connect
# ============================================
function Connect-SSH {
    Write-Info "Connecting to server..."
    ssh -p $SERVER_PORT $SSH_TARGET
}

# ============================================
# Show Help
# ============================================
function Show-Help {
    Write-Host "============================================"
    Write-Host "Hospital Platform Remote Deploy (Windows)"
    Write-Host "============================================"
    Write-Host ""
    Write-Host "Server: $SERVER_HOST"
    Write-Host "Path:   $SERVER_PATH"
    Write-Host ""
    Write-Host "Commands:"
    Write-Host "  deploy   - Sync code and deploy services"
    Write-Host "  sync     - Sync code only"
    Write-Host "  start    - Start services"
    Write-Host "  stop     - Stop services"
    Write-Host "  restart  - Restart services"
    Write-Host "  status   - View service status"
    Write-Host "  logs     - View service logs"
    Write-Host "  rebuild  - Rebuild services"
    Write-Host "  init     - Initialize server (install Docker)"
    Write-Host "  ssh      - SSH to server"
    Write-Host "  help     - Show this help"
    Write-Host ""
    Write-Host "Environment variables:"
    Write-Host "  `$env:SERVER_HOST = 'server-ip'"
    Write-Host "  `$env:SERVER_USER = 'ssh-user'"
    Write-Host "  `$env:SERVER_PORT = 'ssh-port'"
    Write-Host "  `$env:SERVER_PATH = 'deploy-path'"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\deploy-remote.ps1 init"
    Write-Host "  .\deploy-remote.ps1 deploy"
    Write-Host ""
}

# ============================================
# Main
# ============================================
switch ($Command) {
    "deploy" { Check-Connection; Deploy }
    "sync" { Check-Connection; Sync-Code }
    "start" { Check-Connection; Start-Services }
    "stop" { Check-Connection; Stop-Services }
    "restart" { Check-Connection; Stop-Services; Start-Services }
    "status" { Check-Connection; View-Status }
    "logs" { Check-Connection; View-Logs }
    "rebuild" { Check-Connection; Rebuild }
    "init" { Check-Connection; Init-Server }
    "ssh" { Check-Connection; Connect-SSH }
    "help" { Show-Help }
    default { Write-Err "Unknown command: $Command"; Show-Help; exit 1 }
}