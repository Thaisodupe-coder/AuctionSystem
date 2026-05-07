-- Khoi tao cau truc Database cho Auction System
// bảng cho user
CREATE TABLE IF NOT EXISTS users (   // 1 DB chỉ có một bảng user
    id VARCHAR(50) PRIMARY KEY,     // khóa chính để DB tìm nhanh chóng giới hạn 50 kí tự
    username VARCHAR(100) UNIQUE NOT NULL,  // tên đăng nhập là duy nhất và không được để trống
    password VARCHAR(255) NOT NULL,         // tương tự
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP  // nếu không điền thì sẽ lấy thời gian hiện tại
);
// bảng cho items
CREATE TABLE IF NOT EXISTS items (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id VARCHAR(50) REFERENCES users(id)   // tham chiếu đến id của người bán
);
// bảng cho auctions
CREATE TABLE IF NOT EXISTS auctions (
    id VARCHAR(50) PRIMARY KEY,
    item_id VARCHAR(50) REFERENCES items(id) NOT NULL,
    seller_id VARCHAR(50) REFERENCES users(id) NOT NULL,
    start_price DECIMAL(19, 2) NOT NULL CHECK (start_price >= 0),   // kiểm tra luôn điều kiện không cần (nếu logic trong .java ngu)
    highest_bid DECIMAL(19, 2) CHECK (highest_bid >= start_price),
    highest_bidder_id VARCHAR(50) REFERENCES users(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL CHECK (end_time > start_time),
    status VARCHAR(20) DEFAULT 'OPEN'
);
// bid_transactions
CREATE TABLE IF NOT EXISTS bid_transactions (
    id VARCHAR(50) PRIMARY KEY,
    auction_id VARCHAR(50) REFERENCES auctions(id) ON DELETE CASCADE, // xóa phiên thì xóa luôn hàng bid_transaction
    bidder_id VARCHAR(50) REFERENCES users(id),
    amount DECIMAL(19, 2) NOT NULL CHECK (amount > 0),
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_auction_id ON bid_transactions(auction_id);
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);