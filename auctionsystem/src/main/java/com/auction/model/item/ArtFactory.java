package main.java.com.auction.model.item;

public class ArtFactory extends ItemFactory {
    @Override
    public Item createItem(String name) {
        return new Art(name);
    }
    
}
