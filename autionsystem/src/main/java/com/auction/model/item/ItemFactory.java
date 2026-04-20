package com.auction.model.item;

public abstract class ItemFactory {
    public abstract Item createItem(String name);

    public Item prepareForAuctionItem(String name) {
        Item item = createItem(name);
        System.out.println("Sẵn sàng đấu giá sản phẩm: " + item.getName() + " (ID: " + item.getId() + ")");
        return item;
    }
}
