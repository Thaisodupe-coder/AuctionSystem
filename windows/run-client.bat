@echo off
title Auction Client

REM Chuyen huong lam viec ve thu muc goc cua du an
cd /d "%~dp0\.."

echo =====================================================
echo        KHOI DONG AUCTION SYSTEM CLIENT
echo =====================================================

set JAR_FILE=auctionsystem\target\auctionsystem-1.5-SNAPSHOT-jar-with-dependencies.jar

if not exist "%JAR_FILE%" (
    echo LOI: Khong tim thay file he thong .jar!
    echo Vui long chay file 'run-server.bat' truoc de bien dich.
    pause
    exit /b
)

echo Dang mo giao dien Client...
java -cp "%JAR_FILE%" com.auction.Launcher
pause