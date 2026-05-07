package com.auction.service;

import com.auction.model.user.*;
import com.auction.repository.UserRepository;
import com.auction.repository.impl.JdbcUserRepository;
import java.util.Optional;

//quản lý các logic liên quan đến người dùng
public class UserManager {
    private final UserRepository userRepository;
    private static volatile UserManager INSTANCE;

    private UserManager(){
        this.userRepository = new JdbcUserRepository();
        // Seed data: Kiểm tra và tạo tài khoản admin nếu chưa tồn tại trong DB
        if (!userRepository.existsByUsername("admin")) {
            NormalUser adminUser = new NormalUser("admin", "123");
            userRepository.save(adminUser);
        }
    }
    public static UserManager getINSTANCE(){
        if (INSTANCE==null){
            synchronized(UserManager.class){
                if (INSTANCE==null){
                    INSTANCE =new UserManager();
                }
            }
        }
        return INSTANCE;
    }

    // Đăng ký: khi Controller đã xử lý xong các lỗi:name trống; password chứa " ",...
    //chỉ tập trung xử lý logic nghiệp vụ lõi (chống trùng lặp dữ liệu)
    public NormalUser register(String username, String password) throws IllegalArgumentException {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập '" + username + "' đã được sử dụng. Vui lòng chọn tên khác!");
        }

        NormalUser newUser = new NormalUser(username, password);
        
        userRepository.save(newUser);
        return newUser;
    }
    // Đăng nhập: tìm user và đối chiếu mật khẩu, giả sử Controller đã xử lý các lỗi :password trống, name trống;...
    public NormalUser login(String username, String password) throws IllegalArgumentException {
        Optional<NormalUser> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Tài khoản không tồn tại!");
        }

        NormalUser user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Sai mật khẩu!");
        }
        return user;
    }

    // Các hàm cấp phát vai trò cụ thể
    // Trả về đích danh class chức năng, Controller sử dụng

    public Bidder getBidderRole(NormalUser user) {
        if (user == null) throw new IllegalArgumentException("User không được để trống");
        return new Bidder(user);}
    public Seller getSellerRole(NormalUser user) {
        if (user == null) throw new IllegalArgumentException("User không được để trống");
        return new Seller(user);}
    public Admin getAdminRole(NormalUser user) {
        if (user == null) throw new IllegalArgumentException("User không được để trống");
        return new Admin(user);
    }
}
