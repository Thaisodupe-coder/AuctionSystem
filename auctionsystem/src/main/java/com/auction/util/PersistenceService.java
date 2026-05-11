package com.auction.util;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.auction.BidTransaction;
import com.auction.model.item.Art;
import com.auction.model.user.NormalUser;
import com.auction.model.user.Seller;
import com.auction.service.AuctionManager;
import com.auction.service.UserManager;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PersistenceService {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/auctionsystem";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "admin"; 
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    /**
     * Tự động nạp dữ liệu từ PostgreSQL vào các Map private của Manager
     */
    public static void loadData() {
        try (Connection conn = getConnection()) {
            // 1. Load Người dùng
            Map<String, NormalUser> userMap = new HashMap<>();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
                while (rs.next()) {
                    NormalUser user = new NormalUser(rs.getString("username"), rs.getString("password"));
                    setPrivateField(user, "id", rs.getString("id"));
                    user.setBalance(rs.getDouble("balance")); // double không sợ null, mặc định là 0.0
                    userMap.put(user.getName(), user);
                }
            }
            injectToManager(UserManager.getINSTANCE(), "users", userMap);

            // 2. Load Các phiên đấu giá
            Map<String, Auction> auctionMap = new HashMap<>();
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM auctions")) {
                while (rs.next()) {
                    Art item = new Art(rs.getString("item_name"), rs.getString("item_description"));
                    NormalUser owner = UserManager.getINSTANCE().getUserById(rs.getString("seller_id"));
                    if (owner == null) continue;

                    Seller seller = new Seller(owner);
                    
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
            injectToManager(AuctionManager.getINSTANCE(), "auctions", auctionMap);

            // 3. Load Lịch sử đặt giá (Bids)
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM bids ORDER BY bid_time ASC")) {
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

            System.out.println("[Persistence] Hoàn tất nạp dữ liệu từ PostgreSQL.");
        } catch (Exception e) {
            System.err.println("[Persistence] Lỗi khi nạp dữ liệu từ DB: " + e.getMessage());
            throw new RuntimeException(e); // Ném lỗi để bài Test bị Fail thay vì chạy tiếp
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
                               "ON CONFLICT (id) DO UPDATE SET balance = EXCLUDED.balance, password = EXCLUDED.password";
            try (PreparedStatement pstmt = conn.prepareStatement(userUpsert)) {
                for (NormalUser user : UserManager.getINSTANCE().getAllUsers().values()) {
                    pstmt.setString(1, user.getId());
                    pstmt.setString(2, user.getName());
                    pstmt.setString(3, user.getPassword());
                    pstmt.setDouble(4, user.getBalance());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // 2. Lưu Auctions
            String auctionUpsert = "INSERT INTO auctions (id, item_name, item_description, seller_id, highest_bidder_id, highest_bid, start_time, end_time, status) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                                  "ON CONFLICT (id) DO UPDATE SET highest_bidder_id = EXCLUDED.highest_bidder_id, highest_bid = EXCLUDED.highest_bid, status = EXCLUDED.status";
            try (PreparedStatement pstmt = conn.prepareStatement(auctionUpsert)) {
                for (Auction a : AuctionManager.getINSTANCE().getAllAuctions().values()) {
                    pstmt.setString(1, a.getId());
                    pstmt.setString(2, a.getItem().getName());
                    pstmt.setString(3, a.getItem().getDescription());
                    pstmt.setString(4, a.getSeller().getId());
                    pstmt.setString(5, a.getHighestBidderId());
                    pstmt.setDouble(6, a.getHighestBid());
                    pstmt.setTimestamp(7, Timestamp.valueOf(a.getStartTime()));
                    pstmt.setTimestamp(8, Timestamp.valueOf(a.getEndTime()));
                    pstmt.setString(9, a.getStatus().name());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // 3. Lưu Bids (Chỉ thêm những bid mới, không cần update vì bid là lịch sử cố định)
            String bidInsert = "INSERT INTO bids (auction_id, bidder_id, amount, bid_time) " +
                               "SELECT ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM bids WHERE auction_id = ? AND bidder_id = ? AND amount = ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(bidInsert)) {
                for (Auction a : AuctionManager.getINSTANCE().getAllAuctions().values()) {
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
            System.out.println("[Persistence] Hoàn tất lưu dữ liệu vào PostgreSQL.");
        } catch (Exception e) {
            System.err.println("[Persistence] Lỗi khi lưu dữ liệu DB: " + e.getMessage());
            throw new RuntimeException(e); // Ném lỗi để bài Test bị Fail
        }
    }

    private static void injectToManager(Object manager, String fieldName, Map<?, ?> data) throws Exception {
        Field field = manager.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Map targetMap = (Map) field.get(manager);
        targetMap.clear();
        targetMap.putAll(data);
    }

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
        throw new NoSuchFieldException("Không tìm thấy trường " + fieldName + " trong đối tượng " + obj.getClass().getName());
    }
}
