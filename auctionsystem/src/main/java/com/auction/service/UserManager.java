package com.auction.service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//quản lý các logic liên quan đến người dùng
import com.auction.util.PersistenceService;
import com.auction.model.user.*;
public class UserManager {
    private Map<String,NormalUser> users = new ConcurrentHashMap<>();
    private static volatile UserManager INSTANCE;
    private UserManager(){}
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
        NormalUser newUser = new NormalUser(username, password);
        // putIfAbsent(ConcurrentHashMap) :Trả về null nếu chưa có ai lấy tên này, trả về user cũ nếu đã tồn tại
        NormalUser existingUser = users.putIfAbsent(username, newUser);
        //kiểm tra nhiều luồng cùng lúc register cùng tên đăng nhập
        if (existingUser != null) {
            throw new IllegalArgumentException("Tên đăng nhập '" + username + "' đã được sử dụng. Vui lòng chọn tên khác!");
        }
        return newUser;
    }
    // Đăng nhập: tìm user và đối chiếu mật khẩu, giả sử Controller đã xử lý các lỗi :password trống, name trống;...
    public NormalUser login(String username, String password) throws IllegalArgumentException {
        NormalUser user = users.get(username);
        if (user == null) {
            throw new IllegalArgumentException("Tài khoản không tồn tại!");
        }
        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Sai mật khẩu!");
        }
        return user;
    }
    
    // Hàm tìm kiếm User theo ID
    public NormalUser getUserById(String id) {
        for (NormalUser user : users.values()) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
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


    public void addBalance(String userId, double amount) {
        NormalUser user = getUserById(userId);
        if (user != null) {
            user.setBalance(user.getBalance() + amount);
            PersistenceService.saveUser(user);
        }
    }

    /**
     * Trả về danh sách tất cả người dùng (Dùng để kiểm tra/debug)
     */
    public Map<String, NormalUser> getAllUsers() {
        return users;
    }
}