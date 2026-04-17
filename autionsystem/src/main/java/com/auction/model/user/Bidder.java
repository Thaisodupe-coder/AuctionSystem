package com.auction.model.user;
import com.auction.model.auction.*;
import com.auction.service.AuctionManager;

public class Bidder extends User implements IBidder,AuctionObserver{
    public Bidder(String name, String password){
        super(name, password, UserRole.BIDDER);
    }
    @Override
    //đặt giá
    public boolean placeBid(String auctionId, double amount) {
        return AuctionManager.getINSTANCE().placeBid(auctionId, this.getId(), amount);
    }
    /////test thử trong main
    public void update(Auction auction){
        System.out.println(this.getName()+"| cập nhật thông báo mới | giá hiện tại của phiên đấu giá: "+auction.getHighestBid());
    }
}
