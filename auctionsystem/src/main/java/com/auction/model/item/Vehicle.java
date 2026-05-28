package com.auction.model.item;

public class Vehicle extends Item {
    public Vehicle(String name, String description) {
        super(name, description);
    }
    @Override
    public String getCategory() {
        return "Vehicle";
    }
}
