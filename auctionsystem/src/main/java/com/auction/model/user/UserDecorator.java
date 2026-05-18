package com.auction.model.user;

/**Decorator Pattern
 * Dùng để "bọc" một đối tượng User gốc và cung cấp các chức năng mở rộng.
 */
public abstract class UserDecorator extends User {
    protected User user;
    public UserDecorator(User user) {
        super(user.getName(), user.getPassword());
        this.user = user;
    }
    @Override
    public String getId() {
        //trả về ID của user gốc thay vì ID sinh tự động của Decorator
        return user.getId();
    }
    @Override
    public String getName() {
        return user.getName();
    }
    @Override
    public double getBalance() {
        return user.getBalance();
    }
    @Override
    public void setBalance(double balance) {
        user.setBalance(balance);
    }
    // Cho phép lấy lại đối tượng gốc nếu cần thiết
    public User getUser() {
        return user;
    }
}
