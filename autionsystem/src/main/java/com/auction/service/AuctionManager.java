package com.auction.service;
import java.time.LocalDateTime;
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
    public Auction createAuction(Item item, Seller seller, double startPrice, LocalDateTime startTime, LocalDateTime endTime) {
        Auction auction = new Auction(item, seller, startPrice, startTime, endTime);
        updateAuctionStatus(auction);   // gọi phương thức để cài đăt status
        this.addAuction(auction);
        return auction;
    }

    /**
     * Cập nhật trạng thái dựa trên thời gian hiện tại
     */
    public void updateAuctionStatus(Auction auction) {
        if (auction.getStatus() == AuctionStatus.CANCELED || auction.getStatus() == AuctionStatus.PAID) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(auction.getStartTime())) {
            auction.setStatus(AuctionStatus.OPEN);
        } else if (now.isAfter(auction.getEndTime())) {
            auction.setStatus(AuctionStatus.FINISHED);
        } else {
            auction.setStatus(AuctionStatus.RUNNING);
        }
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

        updateAuctionStatus(auction);

        if (auction.getStatus() != AuctionStatus.RUNNING) {
            throw new AuctionClosedException("Chỉ có thể đặt giá khi phiên đấu giá đang RUNNING | Current status: " + auction.getStatus());
        }
        if (amount <= auction.getHighestBid()) {
            throw new InvalidBidException("Bid amount (" + amount + ") must be higher than current highest bid (" + auction.getHighestBid() + ").");
        }
        //xử lý đấu giá **
        auction.setHighestBid(amount); 
        auction.setHighestBidderId(bidderId);
        BidTransaction newBid = new BidTransaction(auction.getId(), bidderId, amount, LocalDateTime.now());
        auction.addBidToHistory(newBid);

        // thông báo tới mọi người
        auction.notifyObservers();
        return true;
    }

    public synchronized boolean cancelAuction(String auctionId, String sellerId) {
        Auction auction = getAuction(auctionId);
        updateAuctionStatus(auction);
        if (auction == null || !auction.getSeller().getId().equals(sellerId) || auction.getStatus() != AuctionStatus.RUNNING) {
            return false;
        }
        auction.setStatus(AuctionStatus.CANCELED);

        // thông báo tới mọi người
        auction.notifyObservers();
        return true;
    }

    /**
     * Trả về ID người chiến thắng nếu phiên đấu giá đã kết thúc thành công
     */
    public String getWinner(String auctionId) {
        Auction auction = getAuction(auctionId);
        if (auction == null) return null;

        // Cập nhật trạng thái trước khi kiểm tra
        updateAuctionStatus(auction);

        // Chỉ có người thắng khi trạng thái là FINISHED hoặc PAID
        if (auction.getStatus() == AuctionStatus.FINISHED || auction.getStatus() == AuctionStatus.PAID) {
            return auction.getHighestBidderId();
        }
        return null; // Hoặc ném Exception nếu phiên chưa kết thúc
    }

    /**
     * Trả về số tiền thắng cược cuối cùng
     */
    public double getWinningAmount(String auctionId) {
        Auction auction = getAuction(auctionId);
        if (auction == null) return 0.0;

        updateAuctionStatus(auction);

        if (auction.getStatus() == AuctionStatus.FINISHED || auction.getStatus() == AuctionStatus.PAID) {
            return auction.getHighestBid();
        }
        return 0.0;
    }
}
