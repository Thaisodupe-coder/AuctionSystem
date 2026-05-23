@echo off
title Auction System Runner
set BASE_DIR=%~dp0
cd /d "%BASE_DIR%\auctionsystem"

echo ======================================================
echo   KHOI DONG HE THONG DAU GIA (AUTOMATED RUNNER)
echo ======================================================

echo [1/4] Dang khoi dong Database (Docker)...
call mvn docker-compose:up

echo [2/4] Dang bien dich ma nguon...
call mvn compile

echo [3/4] Dang mo Server trong cua so moi...
start "Auction Server" cmd /k "mvn exec:java"

echo Dang doi Server on dinh (5 giay)...
timeout /t 5 /nobreak > nul

echo [4/4] Dang khoi dong Client...
start "Auction Client" cmd /k "mvn javafx:run"

echo ======================================================
echo Thanh cong! Ban co the chay them 'mvn javafx:run' 
echo trong thu muc 'auctionsystem' de mo them client moi.
echo ======================================================
pause