package com.auction.model.auction;

import com.auction.model.common.Entity;

public class BidTransaction extends Entity {
    private String auctionId;   
    private String bidderId;
    private double amount;  
    private long timestamp;
    /**
     * thông tin của lần đặt giá
     * @param auctionId id phiên đấu giá
     * @param bidderId id người đặt giá đó
     * @param amount    giá tiền đặt
     * @param timestamp tgian
     */
    public BidTransaction(String auctionId,String bidderId,double amount,long timestamp){
        this.auctionId = auctionId;
        this.bidderId = bidderId;
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
    public double getAmount() {
        return amount;
    }
    public long getTimestamp() {
        return timestamp;
    }
}