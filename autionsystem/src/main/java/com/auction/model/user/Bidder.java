package com.auction.model.user;
import com.auction.model.auction.*;

public class Bidder extends User implements IBidder{
    public Bidder(String name, String password) {
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
        if (auction.getStatus() != AuctionStatus.OPEN) {
            return false;
        }

        if (amount > auction.getHighestBid()) {
            auction.setHighestBid(amount);
            auction.setHighestBidderId(getId());
            return true;
        }
        else return false;
    }
}
