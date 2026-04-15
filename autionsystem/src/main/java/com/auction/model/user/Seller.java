package com.auction.model.user;
import com.auction.model.auction.Auction;
import com.auction.model.item.Item;
import com.auction.service.AuctionManager;

public class Seller extends User implements ISeller {
    public Seller(String name, String password) {
        super(name, password, UserRole.SELLER);
    }

    @Override
    public Auction postItem(Item item, double startPrice, long startTime, long endTime) {
        return AuctionManager.getINSTANCE().createAuction(item, this, startPrice, startTime, endTime);
    }
}
