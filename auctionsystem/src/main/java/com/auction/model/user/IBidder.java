package com.auction.model.user;


public interface IBidder {
    /**
     * Đặt mức giá mới cho phiên đấu giá
     * @param auctionId : id phiên đấu giá tham gia
     * @param amount : số tiền đấu giá
     * @return : true nếu đặt giá hợp lệ , false thì không hợp lệ
     */
    boolean placeBid(String auctionId, double amount);
}
