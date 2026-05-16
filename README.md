# Hướng Dẫn Tải và Cài Đặt Docker Desktop

Tài liệu này hướng dẫn nhanh cách tải và cài đặt Docker Desktop trên các hệ điều hành Windows, Mac và Linux.

---

## 🚀 Bước 1: Tải bộ cài đặt

Bạn luôn luôn nên tải Docker Desktop từ trang chủ để có phiên bản mới nhất và an toàn nhất:

👉 **[Tải Docker Desktop tại đây](https://docker.com)**

*Chọn phiên bản phù hợp với hệ điều hành của bạn (Windows, Mac chip Intel/Apple Silicon, hoặc Linux).*

---

## 🛠️ Bước 2: Cài đặt theo hệ điều hành

### 1. Microsoft Windows
> **Yêu cầu bắt buộc**: Đảm bảo máy tính đã bật tính năng **Hyper-V** hoặc **WSL 2** (khuyến khích dùng WSL 2).

1. Nhấp đúp vào tệp `Docker Desktop Installer.exe` vừa tải về.
2. Tích chọn **"Use WSL 2 instead of Hyper-V"** (nếu có hệ thống hỗ trợ).
3. Bấm **OK** và chờ quá trình giải nén hoàn tất.
4. Bấm **Close and restart** để khởi động lại máy tính.

### 2. Apple macOS
1. Nhấp đúp vào tệp `.dmg` đã tải về để mở.
2. Kéo và thả biểu tượng **Docker** vào thư mục **Applications**.
3. Mở **Applications** và nhấp đúp vào **Docker** để chạy lần đầu tiên.
4. Xác nhận quyền bảo mật của macOS nếu được hệ thống hỏi.

### 3. Linux (Ubuntu / Debian)
1. Cập nhật kho ứng dụng:
   ```bash
   sudo apt-get update
   ```
2. Cài đặt gói tệp `.deb` vừa tải về:
   ```bash
   sudo apt-get install ./docker-desktop-<phiên_bản>-amd64.deb
   ```
3. Khởi động dịch vụ:
   ```bash
   systemctl --user start docker-desktop
   ```

---

## ✅ Bước 3: Kiểm tra cài đặt

Sau khi cài đặt xong, hãy mở công cụ dòng lệnh (Terminal, Command Prompt hoặc PowerShell) và chạy lệnh sau để kiểm tra:

```bash
docker --version
```

Nếu màn hình hiển thị thông tin phiên bản (Ví dụ: `Docker version 27.x.x`), bạn đã cài đặt thành công!

---

## 📝 Tài liệu tham khảo thêm
* [Tài liệu hướng dẫn chính thức của Docker](https://docker.com)
* [Khắc phục lỗi cài đặt Docker Desktop](https://docker.comdesktop/troubleshoot/overview/)
