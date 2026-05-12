package com.auction.network;

import com.auction.model.auction.Auction;
import com.auction.model.item.Art;
import com.auction.model.item.Electronics;
import com.auction.model.item.Item;
import com.auction.model.item.Vehicle;
import com.auction.model.user.NormalUser;
import com.auction.model.user.Seller;
import com.auction.service.AuctionManager;
import com.auction.network.message.Request;
import com.auction.network.message.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import javafx.application.Platform;

public class ClientManager {
    private volatile static ClientManager INSTANCE;
    private Gson gson = new Gson();
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Consumer<Response> responseHandler; // Callback để báo cho Controller biết có kết quả

    private String userId;
    private String userName;

    private ClientManager(){}
    public static ClientManager getINSTANCE(){
        if (INSTANCE==null){
            synchronized(ClientManager.class){
                if (INSTANCE==null){
                    INSTANCE = new ClientManager();
                }
            }
        }
        return INSTANCE;
    }
// kết nối ClientManager với Controller
    public void setResponseHandler(Consumer<Response> responseHandler) {
        this.responseHandler = responseHandler;
    }

    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Đã kết nối tới Server " + host + ":" + port);
            
            startListening();
        } catch (IOException e) {
            System.err.println("Lỗi kết nối tới Server: " + e.getMessage());
        }
    }

    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                String jsonResponse;
                while ((jsonResponse = reader.readLine()) != null) {
                    System.out.println("[Client nhận]: " + jsonResponse);
                    
                    try {
                        Response response = gson.fromJson(jsonResponse, Response.class);
                        
                        // Phân loại: Xử lý ngầm các lệnh Broadcast từ Server
                        if ("NEW_AUCTION_BROADCAST".equals(response.getCommand())) {
                            String aucId = String.valueOf(response.getPayload().get("auctionId"));
                            String itmId = String.valueOf(response.getPayload().get("itemId"));
                            String sellerId = String.valueOf(response.getPayload().get("sellerId"));
                            String sellerName = String.valueOf(response.getPayload().get("sellerName"));
                            String name = String.valueOf(response.getPayload().get("name"));
                            double startPrice = Double.parseDouble(String.valueOf(response.getPayload().get("startPrice")));
                            String category = String.valueOf(response.getPayload().get("category"));
                            String desc = String.valueOf(response.getPayload().get("description"));
                            LocalDateTime endT = LocalDateTime.parse(String.valueOf(response.getPayload().get("endTime")));
                            //xử lý trên client
                            Item localItem;
                            if ("Art".equals(category)) localItem = new Art(name, desc);
                            else if ("Electronics".equals(category)) localItem = new Electronics(name, desc);
                            else if ("Vehicle".equals(category)) localItem = new Vehicle(name, desc);
                            else throw new IllegalArgumentException("Danh mục không hợp lệ");
                            localItem.setId(itmId);

                            NormalUser baseUser = new NormalUser(sellerName, "");
                            baseUser.setId(sellerId);
                            Auction localAuction = new Auction(localItem, new Seller(baseUser), startPrice, LocalDateTime.now(), endT);
                            localAuction.setId(aucId);

                            // Nhét vào RAM của Client
                            AuctionManager.getINSTANCE().addAuction(localAuction);
                            System.out.println("Đã đồng bộ phiên đấu giá mới vào RAM Client thành công!");
                        } else {
                            // Trả về cho Controller (với các Request 1-1 thông thường)
                            if (responseHandler != null) {
                                Platform.runLater(() -> responseHandler.accept(response));
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Lỗi khi Client đọc dữ liệu ngầm: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            } catch (IOException e) {
                System.out.println("Đã ngắt kết nối với Server.");
            }
        });
        listenerThread.setDaemon(true); // Đảm bảo thread tự tắt khi ứng dụng đóng
        listenerThread.start();
    }

    public void sendRequest(Request request) {
        if (writer != null) {
            new Thread(() -> {
                String json = gson.toJson(request);
                writer.println(json);
                System.out.println("[Client gửi]: " + json);
            }).start();
        } else {
            System.err.println("Chưa kết nối tới Server. Không thể gửi request!");
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUser(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public void clearUser() {
        this.userId = null;
        this.userName = null;
    }
}
