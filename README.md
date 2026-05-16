# 🚀 Hướng Dẫn Khởi Chạy Dự Án AuctionSystem

Tài liệu này hướng dẫn chi tiết từ bước chuẩn bị Docker cho đến khi khởi chạy thành công ứng dụng Java kết nối cơ sở dữ liệu PostgreSQL.

---

## 🛠️ Bước 1: Tải và Khởi Động Docker

Ứng dụng sử dụng PostgreSQL chạy trên môi trường ảo hóa, do đó bạn cần chuẩn bị Docker trước.

### 1. Tải xuống Docker Desktop
* Truy cập trang chủ [Docker Desktop](https://docker.com).
* Chọn phiên bản phù hợp với hệ điều hành của bạn (**Windows**, **Mac**, hoặc **Linux**).
* Tải về và tiến hành cài đặt theo hướng dẫn.

### 2. Mở ứng dụng Docker
* Tìm kiếm và mở ứng dụng **Docker Desktop** trên máy tính.
* Chờ cho trạng thái ứng dụng báo **Engine running** (màu xanh lá cây).
* *Lưu ý: Giữ Docker Desktop chạy trong suốt quá trình chạy dự án.*

---

## 📋 Bước 2: Di Chuyển Thư Mục và Chạy Lệnh

Hãy mở **Terminal / Command Prompt** trên máy tính của bạn và thực hiện theo đúng thứ tự sau:

### 1. Di chuyển vào thư mục chứa mã nguồn dự án
Bạn bắt buộc phải đứng tại thư mục `AuctionSystem/auctionsystem` (nơi chứa file `pom.xml` và `docker-compose.yml`) thì các lệnh Maven mới hoạt động:
```bash
cd AuctionSystem/auctionsystem
```

### 2. Khởi động cơ sở dữ liệu PostgreSQL
Lệnh này tự động kích hoạt container chứa database PostgreSQL ngầm định:
```bash
mvn docker-compose:up
```

### 3. Biên dịch mã nguồn Java
Lệnh này thực hiện kiểm tra và biên dịch toàn bộ code Java của hệ thống đấu giá:
```bash
mvn compile
```

### 4. Chạy ứng dụng
Lệnh này khởi chạy ứng dụng Java AuctionSystem để kết nối trực tiếp vào database PostgreSQL:
```bash
mvn exec:java
```

---

## 🛑 Cách Dừng Dự Án
Khi muốn tắt ứng dụng và giải phóng tài nguyên, hãy chạy lệnh sau tại thư mục dự án:
```bash
mvn docker-compose:down
```
