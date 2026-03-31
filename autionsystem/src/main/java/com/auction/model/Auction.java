package com.auction.model;
/**
 * quản lý phiên đấu giá
 */
public class Auction {
    private String itemId;
    private String sellerId;   
    private  String bidderId; //id của bidder trả giá cao nhất
    private double highestPrice;
    private long endTime;
}
