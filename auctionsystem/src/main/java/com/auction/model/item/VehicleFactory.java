package com.auction.model.item;

public class VehicleFactory extends ItemFactory {
    @Override
    public Item createItem(String name) {
        return new Vehicle(name);
    }
}