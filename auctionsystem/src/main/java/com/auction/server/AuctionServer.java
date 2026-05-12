package com.auction.server;

import com.auction.model.auction.Auction;
import com.auction.model.user.NormalUser;
import com.auction.service.AuctionManager;
import com.auction.service.UserManager;
import com.auction.util.PersistenceService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {
    private static final int PORT = 8888;
    // Danh sách lưu trữ các luồng kết nối tới Client (Sẽ dùng cho tính năng Broadcast/Observer sau này)
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    // ThreadPool để quản lý và tái sử dụng các luồng, tránh quá tải server
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        // In ra thư mục làm việc để kiểm tra đường dẫn tương đối
        System.out.println("Working Directory hiện tại: " + System.getProperty("user.dir"));

        System.out.println("\n[Server] Khởi động hệ thống lưu trữ PostgreSQL...");
        PersistenceService.loadData();

        System.out.println("========== KIỂM TRA DỮ LIỆU HỆ THỐNG ==========");
        System.out.println("[USER] Danh sách người dùng:");
        UserManager.getINSTANCE().getAllUsers().values().forEach(u -> 
            System.out.println("  - ID: " + u.getId() + " | Tên: " + u.getName() + " | Số dư: " + u.getBalance()));

        System.out.println("[AUCTION] Danh sách phiên đấu giá:");
        AuctionManager.getINSTANCE().getAllAuctions().forEach(a -> 
            System.out.println("  - ID: " + a.getId() + " | Vật phẩm: " + a.getItem().getName() + " | Trạng thái: " + a.getStatus()));
        System.out.println("===============================================\n");

        System.out.println("port : " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Đang chờ kết nối từ Client...");
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Client mới kết nối: " + socket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Lỗi khởi động Server: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }
    
    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
}