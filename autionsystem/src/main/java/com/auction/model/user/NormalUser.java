package com.auction.model.user;

// Đây là lớp thực thể gốc, chỉ chứa thông tin định danh, không có vai trò cụ thể.
public class NormalUser extends User {
    public NormalUser(String name, String password) {
        super(name, password);
    }
}
