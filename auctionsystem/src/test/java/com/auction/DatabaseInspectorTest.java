package com.auction;

import com.auction.service.AuctionManager;
import com.auction.util.PersistenceService;
import org.junit.jupiter.api.Test;
import java.sql.*;

public class DatabaseInspectorTest {

    @Test
    void viewCurrentDatabaseState() {
        System.out.println("\n========== [DATABASE SNAPSHOT] ==========");
        
        try (Connection conn = PersistenceService.getConnection()) {
            // 1. In bảng Users
            printTable(conn, "BẢNG: users", "SELECT id, username, balance FROM users");

            // 2. In bảng Auctions
            printTable(conn, "BẢNG: auctions", "SELECT id, item_name, highest_bid, status FROM auctions");

            // 3. In bảng Bids
            printTable(conn, "BẢNG: bids", "SELECT auction_id, bidder_id, amount, bid_time FROM bids ORDER BY bid_time DESC");

        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn DB: " + e.getMessage());
        }
        
        System.out.println("=========================================\n");
    }

    private void printTable(Connection conn, String title, String sql) throws SQLException {
        System.out.println("\n" + title);
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // In tiêu đề cột
            printSeparator(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("| %-20s ", metaData.getColumnName(i).toUpperCase());
            }
            System.out.println("|");
            printSeparator(columnCount);

            // In dữ liệu dòng
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    System.out.printf("| %-20s ", value != null ? value.toString() : "NULL");
                }
                System.out.println("|");
            }

            if (!hasData) {
                System.out.println("| (Trống)                                          |");
            }
            printSeparator(columnCount);
        }
    }

    private void printSeparator(int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            System.out.print("+----------------------");
        }
        System.out.println("+");
    }
    
    @Test
    void quickCheckPersistence() {
        AuctionManager mn= AuctionManager.getINSTANCE();
        // Test nhanh: Lưu dữ liệu hiện tại trong RAM xuống và xem kết quả ngay
        System.out.println("[QuickCheck] Đang lưu dữ liệu từ RAM xuống DB...");
        PersistenceService.saveData();
        viewCurrentDatabaseState();
    }
}