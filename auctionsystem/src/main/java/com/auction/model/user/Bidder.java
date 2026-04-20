package com.auction.model.user;
import com.auction.model.auction.*;
import com.auction.service.AuctionManager;
interface IBidder {
    /**
     * Đặt mức giá mới cho phiên đấu giá
     * @param auctionId : id phiên đấu giá tham gia
     * @param amount : số tiền đấu giá
     * @return : true nếu đặt giá hợp lệ , false thì không hợp lệ
     */
    boolean placeBid(String auctionId, double amount);
}
public class Bidder extends UserDecorator implements IBidder,AuctionObserver{
    public Bidder(User decoratedUser){
        super(decoratedUser);
    }
    @Override
    //đặt giá
    public boolean placeBid(String auctionId, double amount) {
        return AuctionManager.getINSTANCE().placeBid(auctionId, this.getId(), amount);
    }
    /////test thử trong main
    public void update(Auction auction){
        System.out.println("[" + this.getName() + "] Thông báo: Vật phẩm '" + auction.getItem().getName() + "' vừa có mức giá mới là: " + auction.getHighestBid());
    }
}
