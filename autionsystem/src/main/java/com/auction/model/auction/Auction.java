package com.auction.model.auction;

import com.auction.model.common.Entity;
import com.auction.model.item.Item;
import com.auction.model.user.Seller;
import com.auction.model.user.User;
import java.util.ArrayList;
import java.util.List;

/**
 * trung tâm quản lý phiên đấu giá
 */
public class Auction extends Entity {
    private Item item;              //sản phẩm đấu giá
    private Seller seller;          //người đấu giá
    private String highestBidderId; //id bidder trả giá cao nhất 
    private double highestBid;      //giá cao nhất tại thời điểm
    private long startTime;         //tgian bắt đầu
    private long endTime;           //tgian kết thúc
    private List<BidTransaction> bidHistory = new ArrayList<>();//lịch sử đấu giá
    private List<AuctionObserver> observers = new ArrayList<>(); //người tham gia đấu giá
    private AuctionStatus status;
    
    public Auction(Item item, Seller seller, double startBid, long startTime, long endTime) {
        super();
        this.item = item;
        this.seller = seller;
        this.highestBidderId = null;
        this.highestBid = startBid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AuctionStatus.OPEN;
    }
    //getter
    public Item getItem() {  return item;  }
    public Seller getSeller() {  return seller;  }
    public String getHighestBidderId() {  return highestBidderId;  }
    public double getHighestBid() {  return highestBid;  }
    public long getStartTime() {  return startTime;  }
    public long getEndTime() {  return endTime;  }
    public AuctionStatus getStatus() {  return status;  }
    //setter
    public void setHighestBid(double highestBid) {  this.highestBid = highestBid;}

    public void setHighestBidderId(String highestBidderId) { this.highestBidderId = highestBidderId; }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public void addBidToHistory(BidTransaction transaction) {
        this.bidHistory.add(transaction);
    }

    //getter BidHistory clone
    public List<BidTransaction> getBidHistory() {
        return new ArrayList<>(bidHistory);
    }
    
    // Observer pattern : dùng cho controller
    public void addObserver(AuctionObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }
    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }
    public void notifyObservers() {
        for (AuctionObserver observer:observers){
            observer.update(this);
        };
    }
}
