package com.auction.model;
import java.util.UUID;
/**
 * cơ sở, cung cấp (id) cho mọi đối tượng
 */
public abstract class Entity {
    private String id;
    
    public Entity(){
        this.id = UUID.randomUUID().toString();
    }
    //setter & getter
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}