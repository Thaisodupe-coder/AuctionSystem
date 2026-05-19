package com.auction.model.auction;

import com.auction.model.common.Entity;
import java.time.LocalDateTime;

public class BidTransaction extends Entity {
    private String auctionId;   
    private String bidderId;
    private String bidderName;
    private double amount;  
    private LocalDateTime timestamp;
    /**
     * thông tin của lần đặt giá
     * @param auctionId id phiên đấu giá
     * @param bidderId id người đặt giá đó
     * @param bidderName tên người đặt giá
     * @param amount    giá tiền đặt
     * @param timestamp tgian
     */
    public BidTransaction(String auctionId, String bidderId, String bidderName, double amount, LocalDateTime timestamp){
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.amount = amount;
        this.timestamp = timestamp;
    }
    //getter
    public String getAuctionId() {
        return auctionId;
    }
    public String getBidderId() {
        return bidderId;
    }
    public String getBidderName() {
        return bidderName;
    }
    public double getAmount() {
        return amount;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}