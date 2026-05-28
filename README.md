# 🔨 Auction System (Hệ Thống Đấu Giá Trực Tuyến)

## 1. Mô tả ngắn gọn bài toán và phạm vi hệ thống
Hệ thống Bidding (đấu giá trực tuyến) là nền tảng phần mềm cho phép nhiều người dùng cùng tham gia cạnh tranh giá để mua một sản phẩm trong một khoảng thời gian xác định. Người bán đưa sản phẩm lên hệ thống và giá bán cuối cùng được xác định thông qua quá trình đấu giá giữa các người mua (Tham khảo mô hình eBay Auctions).

Phạm vi hệ thống bao gồm: Quản lý tài khoản người dùng, Quản lý danh sách sản phẩm đấu giá, Thực hiện đặt giá theo thời gian thực (Real-time bidding), Tự động kết thúc phiên và xử lý thanh toán, lưu trữ dữ liệu an toàn.

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt
### Công nghệ sử dụng
- **Backend:** Java 11, Socket, Đa luồng (Thread Pool, Concurrency, ScheduledExecutorService).
- **Frontend:** JavaFX (Sử dụng CSS để tuỳ chỉnh giao diện Neumorphism).
- **Database:** PostgreSQL, JDBC.
- **DevOps/Tools:** Docker, Docker Compose Maven Plugin, Maven, Git/GitHub.

### Yêu cầu cài đặt (Setup Prerequisites)
1. **Java JDK 11** (hoặc mới hơn, đã được thêm vào biến môi trường `JAVA_HOME`).
2. **Apache Maven** (đã được thêm vào biến môi trường `PATH`).
3. **Docker Desktop** (bắt buộc phải cài đặt, mở và đang chạy ngầm để khởi tạo PostgreSQL database).

## 3. Cấu trúc thư mục hoặc các module chính
Cấu trúc cơ bản của dự án được tổ chức theo mô hình MVC kết hợp Client-Server:
```text
AuctionSystem/
├── windows/                    # Các Script khởi chạy nhanh trên Windows (.bat)
├── linux_macos/                # Các Script khởi chạy nhanh trên Linux/macOS (.sh)
├── docker-compose.yml          # Cấu hình Docker container cho PostgreSQL
├── auctionsystem/
│   ├── pom.xml                 # Cấu hình Maven, dependencies và các plugins (Docker, JavaFX, Exec)
│   └── src/main/
│       ├── java/com/auction/
│       │   ├── Launcher.java       # Lớp trung gian khởi chạy ứng dụng JavaFX (Khắc phục lỗi Module)
│       │   ├── AppClient.java      # Main Class khởi tạo giao diện Client
│       │   ├── server/             # 🖥️ BACKEND: Socket Server, Đa luồng (Multi-threading), ClientHandler
│       │   ├── controller/         # 🎨 FRONTEND: Các Controller xử lý sự kiện UI (ItemDetailsController...)
│       │   ├── network/            # 🌐 MẠNG (Giao tiếp Client-Server):
│       │   │   ├── message/        # Đóng gói dữ liệu truyền tải (Request, Response)
│       │   │   └── ClientManager   # Quản lý luồng Socket phía Client
│       │   ├── model/              # 📦 DỮ LIỆU (Thực thể OOP):
│       │   |   ├── common/         # Chứa duy nhất lớp Thực thể (Entity)
│       │   │   ├── auction/        # Auction, AuctionStatus, BidTransaction
│       │   │   ├── item/           # Lớp trừu tượng Item và các class con (Art, Electronics, Vehicle)
│       │   │   └── user/           # Phân cấp người dùng (NormalUser, Seller...)
│       │   ├── service/            # ⚙️ NGHIỆP VỤ: Các logic xử lý cốt lõi (AuctionManager, UserManager)
│       │   ├── util/               # 🛠️ TIỆN ÍCH: Kết nối DB định kỳ (PersistenceService), Logger
│       │   └── exception/          # ⚠️ NGOẠI LỆ: Xử lý lỗi tuỳ chỉnh (Custom Exceptions)
│       └── resources/
│           ├── db/                 # File init.sql (Script khởi tạo bảng tự động cho PostgreSQL)
│           └── com/auction/client/
│               ├── css/            # Các file định dạng giao diện (style.css - Neumorphism)
│               ├── images/         # Thư mục chứa hình ảnh tài nguyên
│               └── view/           # Các file thiết kế giao diện FXML
```

## 4. Vị trí các file .jar
Sau khi biên dịch và đóng gói thành công bằng lệnh Maven (VD: `mvn clean package`), các file `.jar` sẽ được tạo ra và lưu tại:
- `auctionsystem/target/auctionsystem-1.5-SNAPSHOT.jar` (File jar cơ bản của dự án).
- `auctionsystem/target/auctionsystem-1.5-SNAPSHOT-jar-with-dependencies.jar` (File jar độc lập, đã bao gồm tất cả các thư viện cần thiết để chạy trực tiếp).

## 5. Hướng dẫn chạy hệ thống (How to run)
Hệ thống cung cấp 3 phương thức khởi chạy khác nhau tuỳ thuộc vào mục đích sử dụng. 

**🚨 YÊU CẦU BẮT BUỘC CHUNG:** Bạn **phải mở ứng dụng Docker Desktop** trên máy tính trước. Chờ cho trạng thái Docker chuyển sang **Engine running** rồi mới tiến hành chạy.

### Cách 1: Chạy tự động bằng Script
Khuyến nghị sử dụng cách này để tiết kiệm thời gian. Script sẽ tự động bật Database, biên dịch (nếu cần) và khởi chạy hệ thống:
- **Trên Windows:**
  1. Mở thư mục `windows/`.
  2. Nhấp đúp chuột vào `run-server.bat` để bật DB và Server.
  3. Đợi Server báo sẵn sàng, sau đó nhấp đúp vào `run-client.bat` để mở giao diện.( có thể mở nhiều client cùng lúc)
  4. Khi xong, nhấp đúp vào `stop.bat` để dọn dẹp hệ thống.

- **Trên Linux/macOS:**
  1. Mở Terminal và di chuyển vào thư mục `linux_macos/`.
  2. Chạy lệnh `./run-server.sh` để bật DB và Server.
  3. Mở một **Terminal mới**, di chuyển vào cùng thư mục và chạy `./run-client.sh` để mở giao diện.(có thể mở nhiều client cùng lúc)
  4. Khi xong, chạy lệnh `./stop.sh` để dọn dẹp hệ thống.

### Cách 2: Chạy bản Build sẵn (Dành cho Khách hàng / Nghiệm thu)
Với cách này, bạn không cần cài đặt Maven hay biên dịch lại mã nguồn:
1. Truy cập mục **Releases** trên GitHub của dự án.
2. Tải về **Gói triển khai (Deployment Package)** dạng `.zip` mới nhất và giải nén (Đây là chuẩn đóng gói Portable phổ biến cho các hệ thống Server-Client).
3. Đảm bảo đã bật Docker. Mở thư mục vừa giải nén ra:
   - **Đối với Windows:** Nhấp đúp vào `run-server.bat`, đợi Server báo kết nối DB thành công, sau đó nhấp đúp vào `run-client.bat` để mở giao diện. Khi chạy xong nhấn đúp `stop.bat` để dọn dẹp.
   - **Đối với Linux/macOS:** Chạy lệnh `./run-server.sh`, đợi Server sẵn sàng, mở Terminal mới chạy `./run-client.sh`. Khi chạy xong gõ lệnh `./stop.sh` để dọn dẹp.

### Cách 3: Chạy thủ công bằng lệnh (Dành cho Developer)
Dành cho quá trình phát triển, chạy tuần tự các lệnh sau ở các Terminal khác nhau tại thư mục `auctionsystem`:
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
