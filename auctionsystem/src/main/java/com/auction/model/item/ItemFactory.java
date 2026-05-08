package com.auction.model.item;

public abstract class ItemFactory {
    public abstract Item createItem(String name, String description);

    public Item prepareForAuctionItem(String name, String description) {
        Item item = createItem(name, description);
        System.out.println("Sẵn sàng đấu giá sản phẩm: " + item.getName() + " (ID: " + item.getId() + ")");
        return item;
    }
}