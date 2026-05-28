#!/bin/bash

# Chuyển hướng làm việc về thư mục gốc của dự án
cd "$(dirname "$0")/.." || exit 1

echo "====================================================="
echo "       🚀 KHỞI ĐỘNG AUCTION SYSTEM SERVER 🚀         "
echo "====================================================="

# Kiểm tra xem Docker đã chạy chưa
if ! docker info > /dev/null 2>&1; then
  echo "❌ LỖI: Docker chưa được bật! Vui lòng mở Docker Desktop trước khi chạy script."
  exit 1
fi

echo ""
echo "[1/3] Đang khởi động Database (PostgreSQL)..."
# Chạy docker-compose ở thư mục gốc
docker-compose up -d

echo "⏳ Chờ 5 giây để Database thiết lập kết nối..."
sleep 5

echo ""
echo "[2/3] Đang dọn dẹp, biên dịch và đóng gói mã nguồn..."
# Chuyển vào thư mục chứa pom.xml
cd auctionsystem || { echo "❌ Không tìm thấy thư mục 'auctionsystem'"; exit 1; }
mvn clean package -DskipTests -DdockerCompose.skip=true
cd ..

echo ""
echo "[3/3] Đang khởi chạy Server..."
java -jar auctionsystem/target/auctionsystem-1.5-SNAPSHOT-jar-with-dependencies.jar