package com.auction.network.message;

import java.util.HashMap;
import java.util.Map;

public class Response {
    private String command;
    private String status; //"SUCCESS" hoặc "ERROR"
    private String message;
    private Map<String, Object> payload;

    public Response() {
        this.payload = new HashMap<>();
    }

    public Response(String command, String status, String message) {
        this.command = command;
        this.status = status;
        this.message = message;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
