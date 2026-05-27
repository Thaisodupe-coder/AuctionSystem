package com.auction.model.auction;

import com.auction.model.common.Entity;
import com.auction.model.item.Item;
import com.auction.model.user.Seller;
import com.auction.model.user.NormalUser;
import com.auction.service.UserManager;

import java.time.Duration;
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
        updateAuctionStatus(); // Đảm bảo trạng thái luôn được cập nhật khi truy vấn
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
    private void updateAuctionStatus() {
        // Không thay đổi trạng thái nếu đã ở trạng thái cuối cùng
        if (this.status == AuctionStatus.CANCELED ||
            this.status == AuctionStatus.FINISHED || 
            this.status == AuctionStatus.PAID) {
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
    public synchronized boolean processBid(String bidderId, double amount) {
        updateAuctionStatus(); // Đảm bảo trạng thái hiện tại trước khi xử lý bid
    
        if (this.status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException("Chỉ có thể đặt giá khi phiên đấu giá đang RUNNING | Current status: " + this.status);
        }

        NormalUser user = UserManager.getINSTANCE().getUserById(bidderId);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + bidderId);
        }

        //ngăn chặn người tạo phiên tự đặt giá cho sản phẩm của mình (Shill bidding)
        if (this.seller.getId().equals(bidderId)) {
            throw new InvalidBidException("Bạn không thể tự đặt giá cho phiên đấu giá do chính mình tạo ra!");
        }
        if (user.getBalance() < amount) {
            throw new InvalidBidException("Số dư không đủ! (Yêu cầu: " + amount + ", Hiện có: " + user.getBalance() + ")");
        }
        if (amount <= this.highestBid) {
            throw new InvalidBidException("Bid amount (" + amount + ") must be higher than current highest bid (" + this.highestBid + ").");
        }
        //logic frozen balance
        //trả lại tiền cho người đặt giá cao nhất trước đó nếu có
        //trừ tiền của người đặt giá cao nhất hiện tại
        if (highestBidderId != null){
            UserManager.getINSTANCE().addBalance(highestBidderId, highestBid);
        }
        UserManager.getINSTANCE().addBalance(bidderId, -amount);

        syncBid(bidderId, user.getName(), amount); // Thông báo cho các observer về thay đổi
        return true;
    }

    /**
     * Đồng bộ dữ liệu giá thầu từ Server về Client (Bỏ qua các bước kiểm tra logic của Server).
     * Hàm này được dùng khi Client nhận được tín hiệu Broadcast giá mới.
     */
    public synchronized void syncBid(String bidderId, String bidderName, double amount) {
        //anti-sniping : nếu có bid mới trong 5 phút cuối thì gia hạn phiên thêm 5 phút
        LocalDateTime now = LocalDateTime.now();
        Duration timeLeft = Duration.between(now,this.endTime);
        if (timeLeft.toSeconds() <= 300){
            this.endTime = this.endTime.plusSeconds(300);
        }
        this.highestBid = amount;
        this.highestBidderId = bidderId;
        BidTransaction newBid = new BidTransaction(this.getId(), bidderId, bidderName, amount, LocalDateTime.now());
        this.addBidToHistory(newBid);
        notifyObservers();
    }

    /**
     * Đồng bộ trạng thái từ Server về Client.
     */
    public synchronized void syncStatus(AuctionStatus newStatus) {
        this.status = newStatus;
        notifyObservers();
    }

    public void addBidToHistory(BidTransaction transaction) {
        this.bidHistory.add(transaction);
    }

    //getter BidHistory clone
    public List<BidTransaction> getBidHistory() {
        return new ArrayList<>(bidHistory);
    }
    
    //observer pattern : dùng cho controller
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
        updateAuctionStatus(); // Đảm bảo trạng thái hiện tại

        if (!this.seller.getId().equals(sellerId)) {
            return false; // Chỉ người bán tạo phiên mới có quyền hủy
        }
        if (this.status != AuctionStatus.RUNNING && this.status != AuctionStatus.OPEN) {
            return false; // Chỉ có thể hủy khi đang OPEN hoặc RUNNING
        }
        //hoàn lại tiền đã đóng băng cho người giữ giá cao nhất hiện tại nếu có
        if (this.highestBidderId != null) {
            UserManager.getINSTANCE().addBalance(this.highestBidderId, this.highestBid);
        }
        this.status = AuctionStatus.CANCELED;
        notifyObservers(); // Thông báo cho các observer
        return true;
    }

    /**
     * Chuyển tiền cho Seller
     * Trạng thái từ FINISHED -> PAID
     */
    public synchronized void finalizeAuction() {
        updateAuctionStatus();
        if (this.status == AuctionStatus.FINISHED) {
            // Nếu có người thắng, chuyển tiền đóng băng cho Seller
            if (this.highestBidderId != null) {
                UserManager.getINSTANCE().addBalance(this.seller.getId(), this.highestBid);
            }
            this.status = AuctionStatus.PAID;
            notifyObservers();
        }
    }

    /**
     * Hàm dành cho luồng ngầm để chủ động giám sát và chuyển đổi trạng thái (Real-time)
     */
    public synchronized void monitorState() {
        AuctionStatus oldStatus = this.status;
        updateAuctionStatus();

        // Nếu có sự chuyển đổi trạng thái (VD: OPEN -> RUNNING), lập tức báo cho Client
        if (oldStatus != this.status) {
            notifyObservers(); 
        }
        // Đợi 5 phút sau khi kết thúc (FINISHED) mới tiến hành kết toán và chuyển sang PAID
        if (this.status == AuctionStatus.FINISHED) {
            if (LocalDateTime.now().isAfter(this.endTime.plusMinutes(5))) {
                finalizeAuction();
            }
        }
    }
}