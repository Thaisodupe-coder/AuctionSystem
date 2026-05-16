-- Bảng lưu trữ thông tin người dùng
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    balance DOUBLE PRECISION DEFAULT 0.0
    balance NUMERIC(19, 2) DEFAULT 0.00
);

-- Bảng lưu trữ thông tin các phiên đấu giá
CREATE TABLE IF NOT EXISTS auctions (
    id VARCHAR(255) PRIMARY KEY,
    item_name VARCHAR(255),
    item_type VARCHAR(50),
    item_description TEXT,
    seller_id VARCHAR(255) REFERENCES users(id) ON DELETE RESTRICT, -- Người bán không thể bị xóa nếu còn đấu giá
    highest_bidder_id VARCHAR(255) REFERENCES users(id) ON DELETE SET NULL, -- Nếu người đặt giá cao nhất bị xóa, ID sẽ được đặt thành NULL
    highest_bid DOUBLE PRECISION,
    highest_bid NUMERIC(19, 2),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(50)
);

-- Bảng lưu trữ lịch sử đặt giá (Bid History)
CREATE TABLE IF NOT EXISTS bids (
    id SERIAL PRIMARY KEY,
    auction_id VARCHAR(255) REFERENCES auctions(id) ON DELETE CASCADE, -- Nếu phiên đấu giá bị xóa, các bid liên quan cũng bị xóa
    bidder_id VARCHAR(255) REFERENCES users(id) ON DELETE RESTRICT, -- Người đặt giá không thể bị xóa nếu còn bid
    amount DOUBLE PRECISION,
    amount NUMERIC(19, 2),
    bid_time TIMESTAMP
);
