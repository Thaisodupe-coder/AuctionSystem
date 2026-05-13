package com.auction.model.auction;

/**
 * Enum trạng thái của 1 phiên đấu giá
 */
public enum AuctionStatus {
    OPEN,       // Vừa tạo, chưa bắt đầu
    RUNNING,    // Đang trong thời gian đấu giá
    FINISHED,   // Đã kết thúc thời gian
    CANCELED    // Phiên đấu giá bị hủy
}
