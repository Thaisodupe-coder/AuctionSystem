package com.auction.model.item;

import com.auction.model.common.Entity;

public abstract class Item extends Entity {
    private String name;
    public String getName() {
        return name;
    }
    public Item(String name) {
        super();
        this.name = name;
    }
}