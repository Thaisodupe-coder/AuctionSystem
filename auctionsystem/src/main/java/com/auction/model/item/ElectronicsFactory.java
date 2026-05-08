package com.auction.model.item;

public class ElectronicsFactory extends ItemFactory {
    @Override
    public Item createItem(String name, String description) {
        return new Electronics(name, description);
    }
}
