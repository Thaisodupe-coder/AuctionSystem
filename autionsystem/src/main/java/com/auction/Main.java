package com.auction;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.item.*;
import com.auction.model.user.*;
public class Main {
    public static void main(String[] args) {
        Seller seller = new Seller("vu", "654321");
        Bidder bidder = new Bidder("thai", "123456");
        Item item = new Vehicle("dt");
        Auction auction = seller.postItem(item, 0, 0, 999999);
        bidder.placeBid(auction, 100);
        System.out.println(auction.getHighestBid());
        auction.setStatus(AuctionStatus.FINISHED);
        bidder.placeBid(auction, 989);


    }
}
