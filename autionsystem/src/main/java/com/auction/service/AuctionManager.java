package com.auction.service;
import java.util.ArrayList;
import java.util.List;
import com.auction.model.auction.Auction;
//quản lý các phiên đấu giá
public class AuctionManager {
    private static volatile AuctionManager INSTANCE;
    private List<Auction> auctions = new ArrayList<>();
    private AuctionManager() {}
    public static AuctionManager getINSTANCE() {
        if (INSTANCE == null) {
            synchronized (AuctionManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AuctionManager();
                }
            }
        }
        return INSTANCE;
    }
    public void addAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException("Auction cannot be null");
        }
        auctions.add(auction);
    
    }
}
