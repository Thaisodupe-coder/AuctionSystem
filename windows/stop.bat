@echo off
title Stop Auction System

REM Chuyen huong lam viec ve thu muc goc cua du an
cd /d "%~dp0\.."

echo =====================================================
echo        DANG TAT HE THONG VA DON DEP
echo =====================================================

echo [1/2] Dang dung Container Database (PostgreSQL)...
docker-compose down

echo.
echo [2/2] Don dep cac tep tin da bien dich (target/)...
cd auctionsystem
call mvn clean > nul
cd ..

echo He thong da duoc tat an toan!
pause