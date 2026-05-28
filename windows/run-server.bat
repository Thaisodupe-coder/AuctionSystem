@echo off
title Auction Server

REM Chuyen huong lam viec ve thu muc goc cua du an
cd /d "%~dp0\.."

echo =====================================================
echo        KHOI DONG AUCTION SYSTEM SERVER
echo =====================================================

REM Kiem tra Docker da bat chua
docker info > nul 2> nul
if %errorlevel% neq 0 (
    echo LỖI: Docker chua duoc bat! Vui long mo Docker Desktop truoc khi chay.
    pause
    exit /b
)

echo.
echo [1/3] Dang khoi dong Database (PostgreSQL)...
docker-compose up -d

echo.
echo [2/3] Dang bien dich va dong goi ma nguon...
cd auctionsystem
call mvn clean package -DskipTests -DdockerCompose.skip=true
cd ..

echo.
echo [3/3] Dang khoi chay Server...
java -jar auctionsystem\target\auctionsystem-1.5-SNAPSHOT-jar-with-dependencies.jar