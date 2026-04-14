package com.auction.model.user;
import com.auction.model.auction.*;

public class Bidder extends User implements IBidder{
public class Bidder extends User implements IBidder, AuctionObserver {
    public Bidder(String name, String password){
        super(name, password, UserRole.BIDDER);
    }
    @Override
    /**
     * Đặt mức giá mới cho phiên đấu giá
     * @param auction : phiên đấu giá tham gia
     * @param amount : số tiền đấu giá
     * @return : true nếu đặt giá hợp lệ , false thì không hợp lệ
     */
    public boolean placeBid(Auction auction, double amount) {
        return auction.processBid(this.getId(), amount);
    }
    @Override
    public void update(Auction auction){
        System.out.println("cập nhật thông báo mới"+auction.getHighestBid());
    }
}
