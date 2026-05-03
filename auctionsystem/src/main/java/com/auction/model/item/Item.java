package com.auction.model.item;

import com.auction.model.common.Entity;

public abstract class Item extends Entity {
    private String name;
    private double price;
    private String imagePath;
    
    public String getName() {
        return name;
    }
    public Item(String name) {
        this.name = name;
    }

    public String getCurrentPrice() {
        return getCurrentPrice();
    }

    public String getDescription() {
        return getDescription();
    }
}
