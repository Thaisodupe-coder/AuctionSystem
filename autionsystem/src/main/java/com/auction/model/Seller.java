package com.auction.model;
/**
 * người
 */
public class Seller extends User implements ISeller{
    public Seller(String name,String password){
        super(name, password, UserRole.SELLER);
    }
    @Override
    public Auction postItem(String name,String description,String category, double startPrice,long startTime, long endTime){

    }
}
