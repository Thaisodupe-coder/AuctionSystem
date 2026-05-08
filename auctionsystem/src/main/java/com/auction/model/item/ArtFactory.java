package com.auction.model.item;

public class ArtFactory extends ItemFactory {
    @Override
    public Item createItem(String name, String description) {
        return new Art(name, description);
    }
}
