package com.auction.model.item;

public class VehicleFactory extends ItemFactory {
    @Override
    public Item createItem(String name, String description) {
        return new Vehicle(name, description);
    }
    
}
