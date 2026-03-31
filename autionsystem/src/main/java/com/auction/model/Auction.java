package com.auction.model;
/**
 * quản lý phiên đấu giá
 */
public class Auction {
    private Item item;
    private Seller seller;   
    private  String bidderId; //id của bidder trả giá cao nhất
    private double highestPrice;
    public long startTime;
    private long endTime;
    private AuctionStatus status;
}
