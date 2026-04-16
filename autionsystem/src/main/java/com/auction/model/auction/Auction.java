package com.auction.model.auction;

import com.auction.model.common.Entity;
import com.auction.model.item.Item;
import com.auction.model.user.Seller;
import java.time.LocalDateTime;
import com.auction.exception.AuctionClosedException;
import com.auction.exception.InvalidBidException;
import java.util.ArrayList;
import java.util.List;

/**
 * xử lý các logic liên quan, làm thay đổi trạng thái của phiên đấu giá
 */
public class Auction extends Entity {
    private Item item;              //sản phẩm đấu giá
    private Seller seller;          //người đấu giá
    private String highestBidderId; //id bidder trả giá cao nhất 
    private double highestBid;      //giá cao nhất tại thời điểm
    private LocalDateTime startTime; //tgian bắt đầu
    private LocalDateTime endTime;   //tgian kết thúc
    private List<BidTransaction> bidHistory = new ArrayList<>();//lịch sử đấu giá
    private List<AuctionObserver> observers = new ArrayList<>(); //người tham gia đấu giá
    private AuctionStatus status;
    
    public Auction(Item item, Seller seller, double startBid, LocalDateTime startTime, LocalDateTime endTime) {
        //super();  bỏ super
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
    public LocalDateTime getStartTime() { return this.startTime; }
    public LocalDateTime getEndTime() { return this.endTime; }
    public AuctionStatus getStatus() { 
        updateInternalStatus(); // Đảm bảo trạng thái luôn được cập nhật khi truy vấn
        return this.status; 
    }

    public void setHighestBid(double highestBid) {  this.highestBid = highestBid;}

    public void setHighestBidderId(String highestBidderId) { this.highestBidderId = highestBidderId; }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    /**
 * Cập nhật trạng thái nội bộ của phiên đấu giá dựa trên thời gian hiện tại.
 * Phương thức này là private để đảm bảo chỉ Auction mới có thể tự thay đổi trạng thái của mình.
 */
    private void updateInternalStatus() {
        // Không thay đổi trạng thái nếu đã ở trạng thái cuối cùng
        if (this.status == AuctionStatus.CANCELED || 
            this.status == AuctionStatus.PAID || 
            this.status == AuctionStatus.FINISHED) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(this.startTime)) {
            this.status = AuctionStatus.OPEN;
        } else if (now.isAfter(this.endTime)) {
            this.status = AuctionStatus.FINISHED;
        } else {
            this.status = AuctionStatus.RUNNING;
        }
    }

    /**
 * Xử lý việc đặt giá mới cho phiên đấu giá này.
 * Phương thức này sẽ kiểm tra tính hợp lệ của giá và cập nhật trạng thái nội bộ.
 * @param bidderId ID của người đặt giá.
 * @param amount Số tiền đặt giá.
 * @return true nếu đặt giá thành công.
 * @throws AuctionClosedException nếu phiên đấu giá không ở trạng thái RUNNING.
 * @throws InvalidBidException nếu số tiền đặt giá không hợp lệ (thấp hơn giá hiện tại).
 */
    public synchronized boolean processNewBid(String bidderId, double amount) {
        updateInternalStatus(); // Đảm bảo trạng thái hiện tại trước khi xử lý bid

        if (this.status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException("Chỉ có thể đặt giá khi phiên đấu giá đang RUNNING | Current status: " + this.status);
        }
        if (amount <= this.highestBid) {
            throw new InvalidBidException("Bid amount (" + amount + ") must be higher than current highest bid (" + this.highestBid + ").");
        }
        this.highestBid = amount;
        this.highestBidderId = bidderId;
        BidTransaction newBid = new BidTransaction(this.getId(), bidderId, amount, LocalDateTime.now());
        this.addBidToHistory(newBid);
        notifyObservers(); // Thông báo cho các observer về thay đổi
        return true;
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

    /**
 * Hủy phiên đấu giá này.
 * Chỉ người bán tạo phiên và khi phiên đang ở trạng thái OPEN hoặc RUNNING mới có thể hủy.
 * @param sellerId ID của người yêu cầu hủy.
 * @return true nếu hủy thành công, false nếu không thể hủy.
 */
    public synchronized boolean cancelAuction(String sellerId) {
        updateInternalStatus(); // Đảm bảo trạng thái hiện tại

        if (!this.seller.getId().equals(sellerId)) {
            return false; // Chỉ người bán tạo phiên mới có quyền hủy
        }
        if (this.status != AuctionStatus.RUNNING && this.status != AuctionStatus.OPEN) {
            return false; // Chỉ có thể hủy khi đang OPEN hoặc RUNNING
        }
        this.status = AuctionStatus.CANCELED;
        notifyObservers(); // Thông báo cho các observer
        return true;
    }
}
