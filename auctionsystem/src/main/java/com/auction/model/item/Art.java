package com.auction.model.item;

public class Art extends Item {
    public Art(String name, String description) {
        super(name, description);
    }
    @Override
    public String getCategory() {
        return "Art";
    }
}
