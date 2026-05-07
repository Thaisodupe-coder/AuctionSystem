package com.auction.network.message;

import java.util.HashMap;
import java.util.Map;

public class Request {
    private String command;
    private Map<String, Object> payload;   

    public Request() {this.payload = new HashMap<>();}

    public Request(String command) {
        this.command = command;
        this.payload = new HashMap<>();
    }

    public void addData(String key, Object value) {
        this.payload.put(key, value);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
