package com.auction.util;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.auction.BidTransaction;
import com.auction.model.item.Art;
import com.auction.model.item.Electronics;
import com.auction.model.item.Item;
import com.auction.model.item.Vehicle;
import com.auction.model.user.NormalUser;
import com.auction.model.user.Seller;
import com.auction.service.AuctionManager;
import com.auction.service.UserManager;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PersistenceService {
    private static final String DB_URL = getEnvOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/auctionsystem");
    private static final String DB_USER = getEnvOrDefault("DB_USER", "postgres");
    private static final String DB_PASS = getEnvOrDefault("DB_PASSWORD", "admin");

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Khởi động trình lưu dữ liệu định kỳ.
     */
    public static void startPeriodicSave(int intervalMinutes) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                Logger.info("Hệ thống đang thực hiện lưu dữ liệu định kỳ (" + intervalMinutes + " phút)...");
                saveData();
            } catch (Exception e) {
                Logger.error("Lỗi nghiêm trọng trong quá trình lưu định kỳ: " + e.getMessage());
            }
        }, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);
    }

    public static void stopService() {
        scheduler.shutdown();
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            } catch (SQLException e) {
                if (i == maxRetries - 1)
                    throw e;
                Logger.warn("Chưa kết nối được Database, đang thử lại (" + (i + 1) + "/10)...");
                try {
                    Thread.sleep(3000); // Đợi 3 giây trước khi thử lại
                } catch (InterruptedException ignored) {
                }
            }
        }
        return null;
    }

    /**
     * Tự động nạp dữ liệu từ PostgreSQL vào các Map private của Manager
     */
    public static void loadData() {
        try (Connection conn = getConnection()) {
            // 1. Load Người dùng
            Map<String, NormalUser> userMap = new HashMap<>();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {// tạo
                                                                                                                    // đối
                                                                                                                    // tượng
                                                                                                                    // resultset
                                                                                                                    // chứa
                                                                                                                    // dữ
                                                                                                                    // liệu
                                                                                                                    // dạng
                                                                                                                    // bảng
                                                                                                                    // ảo
                while (rs.next()) {
                    NormalUser user = new NormalUser(rs.getString("username"), rs.getString("password"));
                    setPrivateField(user, "id", rs.getString("id"));// lấy id của Entity
                    user.setBalance(rs.getDouble("balance")); // double không sợ null, mặc định là 0.0
                    userMap.put(user.getName(), user);
                }
            }
            injectToManager(UserManager.getINSTANCE(), "users", userMap);

            // 2. Load Các phiên đấu giá
            Map<String, Auction> auctionMap = new HashMap<>();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM auctions")) {
                while (rs.next()) {// chạy lần lượt mỗi hàng trong DB và lấy thông tin tạo auction rồi tiêm vào
                                   // manager
                    String itemType = rs.getString("item_type");
                    String name = rs.getString("item_name");
                    String desc = rs.getString("item_description");

                    Item item;
                    if ("Electronics".equalsIgnoreCase(itemType)) {
                        item = new Electronics(name, desc);
                    } else if ("Vehicle".equalsIgnoreCase(itemType)) {
                        item = new Vehicle(name, desc);
                    } else {
                        item = new Art(name, desc);
                    }

                    NormalUser owner = UserManager.getINSTANCE().getUserById(rs.getString("seller_id"));// lấy user bằng
                                                                                                        // id vì đã load
                                                                                                        // user vào
                                                                                                        // usermanager
                                                                                                        // trước rồi
                    if (owner == null)
                        continue;

                    Seller seller = new Seller(owner);// tạo đối tượng seller từ đối tượng lấy về từ database

                    // Kiểm tra null cho Timestamp trước khi chuyển đổi
                    Timestamp startTs = rs.getTimestamp("start_time");
                    Timestamp endTs = rs.getTimestamp("end_time");

                    Auction auction = new Auction(item, seller, rs.getDouble("highest_bid"),
                            startTs != null ? startTs.toLocalDateTime() : null,
                            endTs != null ? endTs.toLocalDateTime() : null);

                    setPrivateField(auction, "id", rs.getString("id"));
                    auction.setHighestBidderId(rs.getString("highest_bidder_id"));
                    auction.setStatus(AuctionStatus.valueOf(rs.getString("status")));
                    auctionMap.put(auction.getId(), auction);
                }
            }
            injectToManager(AuctionManager.getINSTANCE(), "auctions", auctionMap);// tiêm vào thuộc tính auctionMap của
                                                                                  // manager

            // 3. Load Lịch sử đặt giá (Bids) chuyển vào lớp auction
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM bids ORDER BY bid_time ASC")) {
                while (rs.next()) {
                    String auctionId = rs.getString("auction_id");
                    Auction auction = AuctionManager.getINSTANCE().getAuction(auctionId);
                    if (auction != null) {
                        Timestamp bidTs = rs.getTimestamp("bid_time");
                        BidTransaction bid = new BidTransaction(auctionId, rs.getString("bidder_id"),
                                rs.getDouble("amount"),
                                bidTs != null ? bidTs.toLocalDateTime() : null);
                        auction.addBidToHistory(bid);
                    }
                }
            }

            Logger.info("Hoàn tất nạp dữ liệu từ PostgreSQL.");
        } catch (Exception e) {
            Logger.error("Lỗi khi nạp dữ liệu từ DB: " + e.getMessage());
            throw new RuntimeException(e); // Ném lỗi để bài Test bị Fail thay vì chạy tiếp
        }
    }

    /**
     * Lưu duy nhất một người dùng mới hoặc cập nhật thông tin người dùng hiện tại.
     * Giúp tối ưu hiệu năng so với việc lưu toàn bộ hệ thống.
     */
    public static void saveUser(NormalUser user) {
        String userUpsert = "INSERT INTO users (id, username, password, balance) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET balance = EXCLUDED.balance, password = EXCLUDED.password";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(userUpsert)) {

            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setDouble(4, user.getBalance());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                Logger.info("Đã đồng bộ User [" + user.getName() + "] vào DB.");
            }
        } catch (SQLException e) {
            Logger.error("Lỗi khi lưu User đơn lẻ: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Lưu hoặc cập nhật thông tin trạng thái của một phiên đấu giá duy nhất.
     * Phương thức này giờ đây chỉ tập trung vào metadata của Auction để tối ưu hiệu
     * năng.
     */
    public static void saveAuction(Auction a) {
        String auctionUpsert = "INSERT INTO auctions (id, item_name, item_description, item_type, seller_id, highest_bidder_id, highest_bid, start_time, end_time, status) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET highest_bidder_id = EXCLUDED.highest_bidder_id, highest_bid = EXCLUDED.highest_bid, status = EXCLUDED.status";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(auctionUpsert)) {
            pstmt.setString(1, a.getId());
            pstmt.setString(2, a.getItem().getName());
            pstmt.setString(3, a.getItem().getDescription());
            pstmt.setString(4, a.getItem().getClass().getSimpleName());
            pstmt.setString(5, a.getSeller().getId());
            pstmt.setString(6, a.getHighestBidderId());
            pstmt.setDouble(7, a.getHighestBid());
            pstmt.setTimestamp(8, a.getStartTime() != null ? Timestamp.valueOf(a.getStartTime()) : null);
            pstmt.setTimestamp(9, a.getEndTime() != null ? Timestamp.valueOf(a.getEndTime()) : null);
            pstmt.setString(10, a.getStatus().name());
            pstmt.executeUpdate();
            Logger.info("Đã đồng bộ trạng thái Auction [" + a.getId() + "] vào DB.");
        } catch (SQLException e) {
            Logger.error("Lỗi khi lưu Auction đơn lẻ: " + e.getMessage());
        }
    }

    /**
     * Lưu một lượt đặt giá (Bid) duy nhất vào cơ sở dữ liệu.
     */
    public static void saveBid(BidTransaction b) {
        String bidInsert = "INSERT INTO bids (auction_id, bidder_id, amount, bid_time) " +
                "SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM bids WHERE auction_id = ? AND bidder_id = ? AND amount = ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(bidInsert)) {
            pstmt.setString(1, b.getAuctionId());
            pstmt.setString(2, b.getBidderId());
            pstmt.setDouble(3, b.getAmount());
            pstmt.setTimestamp(4, Timestamp.valueOf(b.getTimestamp()));
            pstmt.setString(5, b.getAuctionId());
            pstmt.setString(6, b.getBidderId());
            pstmt.setDouble(7, b.getAmount());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Lỗi khi lưu Bid đơn lẻ: " + e.getMessage());
        }
    }

    /**
     * Đồng bộ dữ liệu từ RAM xuống các bảng trong Database
     */
    public static void saveData() {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // 1. Lưu Users
            String userUpsert = "INSERT INTO users (id, username, password, balance) VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT (id) DO UPDATE SET balance = EXCLUDED.balance, password = EXCLUDED.password";// nếu
                                                                                                              // chưa có
                                                                                                              // thì tạo
                                                                                                              // mới có
                                                                                                              // rồi thì
                                                                                                              // cập
                                                                                                              // nhật
                                                                                                              // balance
            try (PreparedStatement pstmt = conn.prepareStatement(userUpsert)) {
                for (NormalUser user : UserManager.getINSTANCE().getAllUsers().values()) {
                    pstmt.setString(1, user.getId());
                    pstmt.setString(2, user.getName());
                    pstmt.setString(3, user.getPassword());
                    pstmt.setDouble(4, user.getBalance());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();// đóng gói các lệnh ghi vào DB thành 1 lần và gửi vào DB
            }

            // 2. Lưu Auctions
            String auctionUpsert = "INSERT INTO auctions (id, item_name, item_description, item_type, seller_id, highest_bidder_id, highest_bid, start_time, end_time, status) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (id) DO UPDATE SET highest_bidder_id = EXCLUDED.highest_bidder_id, highest_bid = EXCLUDED.highest_bid, status = EXCLUDED.status";
            try (PreparedStatement pstmt = conn.prepareStatement(auctionUpsert)) {
                for (Auction a : AuctionManager.getINSTANCE().getAllAuctions()) {
                    pstmt.setString(1, a.getId());
                    pstmt.setString(2, a.getItem().getName());
                    pstmt.setString(3, a.getItem().getDescription());
                    pstmt.setString(4, a.getItem().getClass().getSimpleName());
                    pstmt.setString(5, a.getSeller().getId());
                    pstmt.setString(6, a.getHighestBidderId());
                    pstmt.setDouble(7, a.getHighestBid());
                    pstmt.setTimestamp(8, a.getStartTime() != null ? Timestamp.valueOf(a.getStartTime()) : null);
                    pstmt.setTimestamp(9, a.getEndTime() != null ? Timestamp.valueOf(a.getEndTime()) : null);
                    pstmt.setString(10, a.getStatus().name());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // 3. Lưu Bids (Chỉ thêm những bid mới, không cần update vì bid là lịch sử cố
            // định)
            String bidInsert = "INSERT INTO bids (auction_id, bidder_id, amount, bid_time) " +
                    "SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM bids WHERE auction_id = ? AND bidder_id = ? AND amount = ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(bidInsert)) {
                for (Auction a : AuctionManager.getINSTANCE().getAllAuctions()) {
                    for (BidTransaction b : a.getBidHistory()) {
                        pstmt.setString(1, b.getAuctionId());
                        pstmt.setString(2, b.getBidderId());
                        pstmt.setDouble(3, b.getAmount());
                        pstmt.setTimestamp(4, Timestamp.valueOf(b.getTimestamp()));
                        pstmt.setString(5, b.getAuctionId());
                        pstmt.setString(6, b.getBidderId());
                        pstmt.setDouble(7, b.getAmount());
                        pstmt.addBatch();
                    }
                }
                pstmt.executeBatch();
            }

            conn.commit();
            Logger.info("Hoàn tất lưu dữ liệu vào PostgreSQL.");
        } catch (Exception e) {
            Logger.error("Lỗi khi lưu dữ liệu DB (Đang thực hiện Rollback): " + e.getMessage());
            try (Connection conn = getConnection()) {
                if (conn != null && !conn.getAutoCommit())
                    conn.rollback();
            } catch (SQLException ex) {
                Logger.error("Lỗi khi Rollback: " + ex.getMessage());
            }
            throw new RuntimeException(e); // Ném lỗi để bài Test bị Fail
        }
    }

    // method này phục vụ việc tiêm vào các mânger dữ liệu cập nhật từ DB
    private static void injectToManager(Object manager, String fieldName, Map<?, ?> data) throws Exception {
        Field field = manager.getClass().getDeclaredField(fieldName); // khởi tạo đối tượng Field đại diện cho thuộc
                                                                      // tính fieldName
        field.setAccessible(true);// làm cho có thể truy cập thuộc tính private
        Map targetMap = (Map) field.get(manager);
        targetMap.clear();
        targetMap.putAll(data);// bỏ toàn bộ map từ DB vào các thuộc tính cần thiết của manager
    }

    // method phục vụ việc truy cập và cài đặt các thuộc tính private để set thành
    // các value được lấy về từ Database
    private static void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
                return; // Đã tìm thấy và set xong
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // Tìm tiếp ở class cha
            }
        }
        throw new NoSuchFieldException(
                "Không tìm thấy trường " + fieldName + " trong đối tượng " + obj.getClass().getName());
    }
}
