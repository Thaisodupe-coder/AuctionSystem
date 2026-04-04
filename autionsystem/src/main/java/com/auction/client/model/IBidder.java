package com.auction.model;
public interface IBidder {
    /**
     * Đặt mức giá mới cho phiên đấu giá
     * @param auction : phiên đấu giá tham gia
     * @param amount : số tiền đấu giá
     * @return : true nếu đặt giá hợp lệ , false thì không hợp lệ
     */
    boolean joinBidder(Auction auction,double amount);
}