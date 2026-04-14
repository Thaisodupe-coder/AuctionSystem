package com.auction.model.auction;

import com.auction.model.common.Entity;
import com.auction.exception.AuctionClosedException;
import com.auction.exception.InvalidBidException;
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
    private void setHighestBid(double highestBid) {  this.highestBid = highestBid;}

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
    //getter BidHistory clone
    public List<BidTransaction> getBidHistory() {
        return new ArrayList<>(bidHistory);
    }
    /////
    /**
     * 
     * @param bidderId  id bidder
     * @param amount    bid âmount
     * @return  true nếu kiểm tra thành công, false không thành công
     */
    public synchronized boolean processBid(String bidderId, double amount) {
        if (this.status != AuctionStatus.OPEN && this.status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException("Phiên đấu giá chưa cho phép đấu giá | Current status: " + this.status);
        }
        if (amount <= this.highestBid) {
            throw new InvalidBidException("Bid amount (" + amount + ") must be higher than current highest bid (" + this.highestBid + ").");
        }
        //xử lý đấu giá **
        this.setHighestBid(amount); 
        this.highestBidderId = bidderId; 
        long bidTime = System.currentTimeMillis();
        BidTransaction newBid = new BidTransaction(bidderId, this.getId(), amount, bidTime);
        this.bidHistory.add(newBid);

        // thông báo tới mọi người
        notifyObservers();
        return true;
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
    private void notifyObservers() {
        observers.forEach(observer -> observer.update(this));
    }
}
