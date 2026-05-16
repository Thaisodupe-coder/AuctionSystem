# 🔨 Auction System (Hệ Thống Đấu Giá Trực Tuyến)

Hệ thống đấu giá trực tuyến xây dựng bằng **Java Core (Socket)**, giao diện **JavaFX** và cơ sở dữ liệu **PostgreSQL** (chạy qua Docker Maven Plugin).

---

## 🚀 Hướng Dẫn Khởi Chạy (How to run)

Hệ thống áp dụng kiến trúc Database-First và Client-Server, vui lòng khởi động theo đúng trình tự **Từ dưới lên (Database -> Biên dịch -> Server -> Client)**.

### 🚨 Lưu ý quan trọng trước khi chạy:
Bạn **phải mở ứng dụng Docker Desktop** trên máy tính trước. Chờ cho biểu tượng cá voi chuyển sang **màu xanh lá cây** (Engine running) rồi mới tiến hành chạy các lệnh bên dưới.

### Bước 1: Khởi động Database (PostgreSQL)
Mở Terminal, di chuyển vào đúng thư mục `auctionsystem` (nơi chứa file `pom.xml`) và kích hoạt database thông qua Maven:
```bash
cd AuctionSystem/auctionsystem
mvn docker-compose:up
```
*Lưu ý: Lệnh này sẽ tự động kích hoạt container PostgreSQL ngầm định theo cấu hình hệ thống.*

### Bước 2: Biên dịch mã nguồn Java
Mở một Terminal mới (hoặc dùng tiếp terminal trên nếu lệnh trước chạy ngầm), đảm bảo đang đứng tại thư mục `auctionsystem` và chạy lệnh biên dịch:
```bash
cd AuctionSystem/auctionsystem
mvn compile
```

### Bước 3: Khởi động Server
Tại thư mục `auctionsystem`, chạy lệnh sau để khởi chạy server:
```bash
mvn exec:java
```
*💡 **Dấu hiệu thành công:** Khi terminal hiển thị thông báo khởi tạo server thành công (hoặc đứng im giữ luồng chạy ổn định). Server sẽ kết nối với Database và mở cổng kết nối để chờ Client.*

### Bước 4: Khởi động Client (Giao diện người dùng)
Mở thêm một Terminal mới, di chuyển vào thư mục `auctionsystem` và chạy lệnh:
```bash
cd AuctionSystem/auctionsystem
mvn javafx:run
```
*Bạn có thể mở nhiều Terminal và chạy lệnh này nhiều lần để giả lập nhiều người dùng cùng tham gia đấu giá.*

---

## 🛠️ Hướng Dẫn Tải & Cấu Hình Môi Trường (Setup Prerequisites)

Nếu máy tính của bạn chưa cài đặt các công cụ cần thiết, hãy thực hiện nhanh theo hướng dẫn sau:

### 1. Cài đặt Java JDK 11 (hoặc mới hơn)
* **Tải xuống**: Truy cập trang chủ [Oracle JDK 11](https://oracle.com) hoặc [Amazon Corretto 11](https://amazon.com) để tải bản cài đặt phù hợp (.exe cho Windows, .pkg cho Mac).
* **Thiết lập**: Chạy file vừa tải để cài đặt. Đảm bảo bạn đã thêm đường dẫn JDK vào biến môi trường `JAVA_HOME`.
* **Kiểm tra**: Mở Terminal gõ `java -version` nếu hiển thị đúng phiên bản là thành công.

### 2. Cài đặt Apache Maven
* **Tải xuống**: Truy cập [Maven Download](https://apache.org), tải file Zip (Binary zip archive).
* **Thiết lập**: Giải nén vào một thư mục cố định (ví dụ: `C:\maven`). Thêm đường dẫn thư mục `bin` vào biến môi trường `PATH` của hệ thống.
* **Kiểm tra**: Mở Terminal gõ `mvn -v` để xác nhận hệ thống đã nhận diện được lệnh Maven.

### 3. Cài đặt & Khởi động Docker Desktop
* **Tải xuống**: Truy cập trang chủ [Docker Desktop](https://docker.com) để tải bộ cài đặt phù hợp với hệ điều hành.
* **Cài đặt**: Tiến hành nhấn Next theo trình duyệt cài đặt mặc định (đối với Windows nên tích chọn cài đặt WSL 2 nếu hệ thống yêu cầu).
* **Mở Docker**: Sau khi cài xong, tìm và khởi chạy phần mềm **Docker Desktop**. Chờ vài phút cho đến khi biểu tượng cá voi ở góc dưới màn hình sáng xanh (Engine running). Luôn giữ Docker chạy ngầm khi chạy dự án này.

---

## 🛑 Hướng Dẫn Tắt Hệ Thống An Toàn (Graceful Shutdown)

Để tránh mất mát dữ liệu và lỗi treo hệ thống, vui lòng tắt theo trình tự **Từ trên xuống**:

1. **Tắt Client:** Đóng tất cả các cửa sổ ứng dụng JavaFX (Nhấn nút X trên giao diện).
2. **Tắt Server:** Trở lại Terminal đang chạy Server, nhấn tổ hợp phím `Ctrl + C` để đóng server.
3. **Tắt Database:** Tại terminal đang đứng ở thư mục `auctionsystem`, chạy lệnh sau để dọn dẹp và dừng Container PostgreSQL:
```bash
mvn docker-compose:down
```

---
## 💻 Công nghệ sử dụng
- Backend: Java 11, Socket, Đa luồng (Thread Pool, Concurrency).
- Frontend: JavaFX.
- Database: PostgreSQL, JDBC.
- DevOps: Docker, Docker Compose Maven Plugin, Maven.
