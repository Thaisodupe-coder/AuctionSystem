#!/bin/bash

# Chuyển hướng làm việc về thư mục gốc của dự án
cd "$(dirname "$0")/.." || exit 1

echo "====================================================="
echo "       🖥️ KHỞI ĐỘNG AUCTION SYSTEM CLIENT 🖥️         "
echo "====================================================="

FILE_JAR="auctionsystem/target/auctionsystem-1.5-SNAPSHOT-jar-with-dependencies.jar"

if [ ! -f "$FILE_JAR" ]; then
    echo "❌ LỖI: Không tìm thấy file hệ thống (.jar)!"
    echo "Vui lòng chạy file 'run-server.sh' trước để biên dịch mã nguồn nhé."
    exit 1
fi

echo "Đang mở giao diện Client... (Bạn có thể chạy script này nhiều lần để mở nhiều Client)"
# Ghi đè class thực thi (từ Server sang Client) thông qua tham số -cp
java -cp "$FILE_JAR" com.auction.Launcher &