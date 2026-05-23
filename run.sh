#!/bin/bash

# Di chuyen vao thu muc chua ma nguon
cd "$(dirname "$0")/auctionsystem"

echo "======================================================"
echo "  KHOI DONG HE THONG DAU GIA (LINUX/MAC)              "
echo "======================================================"

echo "[1/4] Dang khoi dong Database (Docker)..."
mvn docker-compose:up

echo "[2/4] Dang bien dich ma nguon..."
mvn compile

echo "[3/4] Dang mo Server..."
mvn exec:java &
SERVER_PID=$!

echo "Dang doi Server on dinh (5 giay)..."
sleep 5

echo "[4/4] Dang khoi dong Client..."
mvn javafx:run

echo "Luu y: Khi tat, hay dam bao dong Server (PID: $SERVER_PID)."