package com.auction;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.item.*;
import com.auction.model.user.*;
public class Main {
    public static void main(String[] args) {
        Seller seller = new Seller("vu", "654321");
        Bidder bidder1 = new Bidder("thai", "123456");
        Bidder bidder2 = new Bidder("hoang", "dfghgfvcagw");
        Item item = new Vehicle("vinfast");
        Auction auction = seller.postItem(item, 0, 0, 999999);
        auction.addObserver(bidder1);
        auction.addObserver(bidder2);
        System.out.println("bắt đầu đấu giá, giá khởi điểm: "+auction.getHighestBid());
        bidder1.placeBid(auction, 100);
        bidder2.placeBid(auction, 989);
        System.out.println(auction.getHighestBid());
        System.out.println(auction.getHighestBidderId());
        auction.setStatus(AuctionStatus.FINISHED);
        bidder2.placeBid(auction, 989);


    }
}
