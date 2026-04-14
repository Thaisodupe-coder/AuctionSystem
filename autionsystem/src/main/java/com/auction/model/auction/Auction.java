package com.auction.model.auction;

import com.auction.model.common.Entity;
import com.auction.model.item.Item;
import com.auction.model.user.Seller;

/**
 * quản lý phiên đấu giá
 */
public class Auction extends Entity {
    private Item item;
    private Seller seller;
    private String highestBidderId; // id của bidder trả giá cao nhất
    private double highestBid;
    private long startTime;
    private long endTime;
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

    public Item getItem() {
        return item;
    }

    public Seller getSeller() {
        return seller;
    }

    public String getHighestBidderId() {
        return highestBidderId;
    }

    public void setHighestBidderId(String highestBidderId) {
        this.highestBidderId = highestBidderId;
    }

    public double getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(double highestBid) {
        this.highestBid = highestBid;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
}
