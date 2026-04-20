package com.auction.model.user;

public class Admin extends UserDecorator{
    public Admin(User decoratedUser){
        super(decoratedUser);
    }
}
