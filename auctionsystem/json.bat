=========================================================
      GIAO THỨC GIAO TIẾP JSON (CLIENT <-> SERVER)
=========================================================

I. CẤU TRÚC GỐC (BẮT BUỘC CHO MỌI TIN NHẮN)
---------------------------------------------------------
{
  "command": "Tên_Lệnh",          // Ví dụ: "LOGIN", "PLACE_BID", "ERROR"
  "status": "SUCCESS / ERROR",    // Trạng thái (Thường dùng khi Server trả lời)
  "message": "Nội dung mô tả",    // Thông báo lỗi nếu status = ERROR, hoặc null
  "payload": {                    // Nơi chứa dữ liệu động tuỳ theo command
      ...
  }
}


II. CÁC KỊCH BẢN CHI TIẾT (VÍ DỤ)
---------------------------------------------------------

1. ĐĂNG NHẬP (LOGIN)
[Client -> Server]
{
  "command": "LOGIN",
  "payload": {
    "username": "nguyenvana",
    "password": "123"
  }
}

[Server -> Client] (Thành công)
{
  "command": "LOGIN_RES",
  "status": "SUCCESS",
  "payload": {
    "userId": "user_01",
    "name": "Nguyen Van A"
  }
}

[Server -> Client] (Thất bại)
{
  "command": "LOGIN_RES",
  "status": "ERROR",
  "message": "Sai mật khẩu!",
  "payload": null
}

---------------------------------------------------------
2. ĐẶT GIÁ (PLACE_BID)
[Client -> Server]
{
  "command": "PLACE_BID",
  "payload": {
    "auctionId": "auc_12345",
    "bidderId": "user_01",
    "amount": 1500000.0
  }
}

[Server -> Client] (Phản hồi cho người vừa đặt giá)
{
  "command": "PLACE_BID_RES",
  "status": "SUCCESS",
  "message": "Đặt giá thành công!",
  "payload": null
}

---------------------------------------------------------
3. SERVER THÔNG BÁO CHO TẤT CẢ (BROADCAST/OBSERVER UPDATE)
[Server -> Tất cả Client đang xem]
{
  "command": "NEW_BID_UPDATE",
  "status": "SUCCESS",
  "payload": {
    "auctionId": "auc_12345",
    "highestBidderId": "user_01",
    "newHighestBid": 1500000.0
  }
}