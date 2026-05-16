# 🚀 Hướng Dẫn Khởi Chạy Dự Án AuctionSystem

Tài liệu này hướng dẫn chi tiết từ bước chuẩn bị Docker cho đến khi khởi chạy thành công ứng dụng Java kết nối cơ sở dữ liệu PostgreSQL.

---

## 🛠️ Bước 1: Tải và Khởi Động Docker (BẮT BUỘC)

Ứng dụng sử dụng PostgreSQL chạy trên Docker. **Bạn phải bật Docker trước khi chạy các lệnh Maven ở Bước 2**, nếu không lệnh sẽ bị lỗi.

### 1. Tải xuống Docker Desktop (Nếu chưa có)
* Truy cập trang chủ [Docker Desktop](https://docker.com).
* Tải về và cài đặt phiên bản phù hợp với hệ điều hành của bạn.

### 2. Mở ứng dụng Docker
* Tìm kiếm và mở ứng dụng **Docker Desktop** trên máy tính của bạn.
* Chờ cho ứng dụng khởi động xong và báo trạng thái **Engine running** (màu xanh lá cây).
* *Lưu ý: Luôn giữ Docker Desktop chạy trong suốt quá trình chạy dự án.*

---

## 📋 Bước 2: Di Chuyển Thư Mục và Chạy Lệnh

Mở **Terminal / Command Prompt**, di chuyển vào đúng thư mục dự án và chạy các lệnh theo thứ tự sau:

### 1. Di chuyển vào thư mục chứa dự án
Bạn bắt buộc phải đứng tại thư mục này (nơi chứa file `pom.xml`) thì lệnh Maven mới hoạt động:
```bash
cd AuctionSystem/auctionsystem
```

### 2. Khởi động cơ sở dữ liệu PostgreSQL
*(Yêu cầu Docker Desktop đã được mở ở Bước 1)*. Lệnh này sẽ tự động kích hoạt database:
```bash
mvn docker-compose:up
```

### 3. Biên dịch mã nguồn Java
Lệnh này thực hiện kiểm tra và biên dịch toàn bộ code Java của hệ thống:
```bash
mvn compile
```

### 4. Chạy ứng dụng
Lệnh này khởi chạy ứng dụng Java AuctionSystem và kết nối vào database PostgreSQL:
```bash
mvn exec:java
```

---

## 🛑 Cách Dừng Dự Án
Khi muốn tắt ứng dụng và giải phóng tài nguyên, hãy chạy lệnh sau tại thư mục dự án:
```bash
mvn docker-compose:down
```
