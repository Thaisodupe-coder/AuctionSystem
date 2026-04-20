package com.auction.model.user;
import com.auction.model.auction.Auction;
import com.auction.model.item.Item;
import com.auction.service.AuctionManager;
import java.time.LocalDateTime;
interface ISeller {
    /**
     * Tạo 1 phiên đấu giá cho sản phẩm
     * @param item : sản phẩm đấu giá
     * @param startPrice : giá tại thời điểm bắt đầu
     * @param startTime : thời gian bắt đầu phiên đấu giá
     * @param endTime : thời gian kết thúc phiên đấu giá
     * @return
     */
    Auction postItem(Item item, double startPrice, LocalDateTime startTime, LocalDateTime endTime);

    boolean cancelAuction(String auctionId);
}
/**
 * bọc lớp NormalUser
 */
public class Seller extends UserDecorator implements ISeller {
    public Seller(User decoratedUser) {
        super(decoratedUser);
    }

    @Override
    public Auction postItem(Item item, double startPrice, LocalDateTime startTime, LocalDateTime endTime) {
        return AuctionManager.getINSTANCE().createAuction(item, this, startPrice, startTime, endTime);
    }

    @Override
    public boolean cancelAuction(String auctionId) {
        return AuctionManager.getINSTANCE().cancelAuction(auctionId, this.getId());
    }
}
