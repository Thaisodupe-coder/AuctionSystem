package com.auction.model.user;

import com.auction.model.common.Entity;

public abstract class User extends Entity {
    private String name;
    private String password;
    private UserRole role;
    private double balance;
    /**
     *
     * @param name tên tài khoản
     * @param password mật khẩu
     * @param role vai trò (buyer, bidder,admin)
     */
    public User(String name, String password) {
        super();
        this.name = name;
        this.password = password;
        this.balance = 0.0; // Mặc định số dư ban đầu là 0
    }

    //getter & setter
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
}
