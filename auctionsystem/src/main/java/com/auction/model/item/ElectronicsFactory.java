package main.java.com.auction.model.item;

public class ElectronicsFactory extends ItemFactory {
    @Override
    public Item createItem(String name) {
        return new Electronics(name);
    }
    
}
