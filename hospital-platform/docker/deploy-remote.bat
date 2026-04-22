@echo off
REM ============================================
REM 医院平台远程部署脚本 (Windows CMD)
REM ============================================
REM 使用方法: deploy-remote.bat [命令]
REM 常用命令: deploy, sync, start, stop, status, logs, ssh
REM ============================================

setlocal enabledelayedexpansion

REM 服务器配置（可修改）
if not defined SERVER_HOST set SERVER_HOST=101.201.30.29
if not defined SERVER_USER set SERVER_USER=root
if not defined SERVER_PORT set SERVER_PORT=22
if not defined SERVER_PATH set SERVER_PATH=/opt/hospital-platform

REM 获取命令
set COMMAND=%1
if "%COMMAND%"=="" set COMMAND=help

echo ============================================
echo 医院平台远程部署
echo ============================================
echo.
echo 服务器: %SERVER_HOST%
echo 用户:   %SERVER_USER%
echo 路径:   %SERVER_PATH%
echo.

REM PowerShell 路径
set PS_SCRIPT=%~dp0deploy-remote.ps1

REM 调用 PowerShell 脚本
powershell -ExecutionPolicy Bypass -File "%PS_SCRIPT%" %COMMAND%

endlocal