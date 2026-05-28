#!/bin/bash

# Chuyển hướng làm việc về thư mục gốc của dự án
cd "$(dirname "$0")/.." || exit 1

echo "====================================================="
echo "       🛑 ĐANG TẮT HỆ THỐNG VÀ DỌN DẸP 🛑           "
echo "====================================================="

echo "[1/2] Đang dừng Container Database (PostgreSQL)..."
docker-compose down

echo ""
echo "[2/2] Dọn dẹp các tệp tin đã biên dịch (target/)..."
cd auctionsystem && mvn clean > /dev/null

echo "✅ Hệ thống đã được tắt an toàn!"