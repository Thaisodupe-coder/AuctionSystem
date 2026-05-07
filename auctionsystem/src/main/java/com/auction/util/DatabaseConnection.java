package com.auction.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // URL: jdbc:postgresql://[host]:[port]/[database_name]
    private static final String URL = "jdbc:postgresql://localhost:5432/auction_system";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin"; 

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found", e);
        }
    }

    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            Logger.error("Error closing connection: " + e.getMessage());
        }
    }
}