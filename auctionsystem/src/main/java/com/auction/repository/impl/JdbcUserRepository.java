package com.auction.repository.impl;

import com.auction.model.user.NormalUser;
import com.auction.repository.UserRepository;
import com.auction.util.DatabaseConnection;
import com.auction.util.Logger;

import java.sql.*;
import java.util.Optional;

/**
 * Triển khai các thao tác cơ sở dữ liệu cho User bằng JDBC.
 */
public class JdbcUserRepository implements UserRepository {

    @Override
    public void save(NormalUser user) {
        // Câu lệnh INSERT vào bảng users
        String sql = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";
        
        // Sử dụng try-with-resources để tự động đóng kết nối
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.executeUpdate();
            Logger.info("Saved user to DB: " + user.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<NormalUser> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Ánh xạ dữ liệu từ ResultSet sang đối tượng Java
                    NormalUser user = new NormalUser(
                        rs.getString("username"),
                        rs.getString("password")
                    );
                    // Lưu ý: Tạm thời dùng constructor này, ID sẽ bị sinh mới ngẫu nhiên.
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            Logger.error("Error finding user: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByUsername(String username) {
        // SELECT 1 để kiểm tra tồn tại một cách tối ưu
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}