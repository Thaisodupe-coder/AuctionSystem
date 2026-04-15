package com.auction.service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.auction.exception.AuctionClosedException;
import com.auction.exception.InvalidBidException;
import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.auction.BidTransaction;
import com.auction.model.item.Item;
import com.auction.model.user.Seller;
//quản lý các phiên đấu giá, xử lý các logic liên quan đến phiên đấu giá
public class AuctionManager {
    private static volatile AuctionManager INSTANCE;
    private Map<String, Auction> auctions = new ConcurrentHashMap<>(); //lưu trữ các phiên đấu giá
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
    //thêm phiên đấu giá vào
    public void addAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException("Auction cannot be null");
        }
        auctions.put(auction.getId(), auction);
    
    }
    public Auction getAuction(String auctionId) {
        return auctions.get(auctionId);
    }
    //tạo phiên đấu giá
    public Auction createAuction(Item item, Seller seller, double startPrice, long startTime, long endTime) {
        Auction auction = new Auction(item, seller, startPrice, startTime, endTime);
        this.addAuction(auction);
        return auction;
    }
    /**
     *  đặt giá cho phiên đấu giá
     * @param auctionId id phiên đấu giá ()
     * @param bidderId  id người đặt giá
     * @param amount    giá trị được đặt
     * @return true nếu đặt giá thành công, false nếu đặt giá thất bại
     */
    public synchronized boolean placeBid(String auctionId, String bidderId, double amount) {
        Auction auction = getAuction(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Auction with ID " + auctionId + " not found.");
        }

        if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING) {
            throw new AuctionClosedException("Phiên đấu giá chưa cho phép đấu giá | Current status: " + auction.getStatus());
        }
        if (amount <= auction.getHighestBid()) {
            throw new InvalidBidException("Bid amount (" + amount + ") must be higher than current highest bid (" + auction.getHighestBid() + ").");
        }
        //xử lý đấu giá **
        auction.setHighestBid(amount); 
        auction.setHighestBidderId(bidderId); 
        long bidTime = System.currentTimeMillis();
        BidTransaction newBid = new BidTransaction(auction.getId(), bidderId, amount, bidTime);
        auction.addBidToHistory(newBid);

        // thông báo tới mọi người
        auction.notifyObservers();
        return true;
    }
}
