package com.auction.network;

import com.auction.model.auction.Auction;
import com.auction.model.auction.BidTransaction;
import com.auction.model.item.*;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.user.NormalUser;
import com.auction.model.user.Seller;
import com.auction.service.AuctionManager;
import com.auction.network.message.Request;
import com.auction.network.message.Response;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.application.Platform;

public class ClientManager {
    private volatile static ClientManager INSTANCE;
    private final Gson gson = new Gson();
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Consumer<Response> responseHandler; // Callback để báo cho Controller biết có kết quả

    private String userId;
    private String userName;
    private double totalBalance;

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
    // chờ nhận các json từ server gửi cho client
    private void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                String jsonResponse;
                while ((jsonResponse = reader.readLine()) != null) {
                    System.out.println("[Client nhận]: " + jsonResponse);
                    
                    try {
                        Response response = gson.fromJson(jsonResponse, Response.class);
                        
                        // Phân loại: Xử lý ngầm các lệnh Broadcast từ Server
                        if ("NEW_AUCTION_BROADCAST".equals(response.getCommand())) { // PUSH
                            addLocalAuction(response.getPayload());
                            System.out.println("___thêm phiên đấu giá mới vào local___");
                        }
                        else if ("NEW_BID_BROADCAST".equals(response.getCommand())) { // PUSH: Nhận lượt bid mới
                            String auctionId = String.valueOf(response.getPayload().get("auctionId"));
                            String bidderId = String.valueOf(response.getPayload().get("bidderId"));
                            String bidderName = String.valueOf(response.getPayload().get("bidderName"));
                            double amount = Double.parseDouble(String.valueOf(response.getPayload().get("amount")));
                            
                            Auction localAuction = AuctionManager.getINSTANCE().getAuction(auctionId);
                            // Cập nhật từ Broadcast cho tất cả các Client (kể cả client vừa gửi)
                            if (localAuction != null) {
                                localAuction.syncBid(bidderId, bidderName, amount);
                                AuctionManager.getINSTANCE().notifyAuctionChanged(); // Bấm chuông báo thay đổi
                            }
                        } else if ("STATUS_UPDATE_BROADCAST".equals(response.getCommand())) { // PUSH: Nhận cập nhật trạng thái
                            String auctionId = String.valueOf(response.getPayload().get("auctionId"));
                            String newStatusStr = String.valueOf(response.getPayload().get("newStatus"));
                            
                            Auction localAuction = AuctionManager.getINSTANCE().getAuction(auctionId);
                            if (localAuction != null) {
                                localAuction.syncStatus(AuctionStatus.valueOf(newStatusStr));
                                AuctionManager.getINSTANCE().notifyAuctionChanged(); // Bấm chuông báo thay đổi
                            }
                        } else if ("GET_ALL_AUCTIONS_RES".equals(response.getCommand())) { // PULL
                            // Xóa dữ liệu cũ trước khi nạp dữ liệu thật
                            AuctionManager.getINSTANCE().clearAuctions();
                            // Dữ liệu trả về là một List các Map
                            List<Map<String, Object>> auctionDataList = (List<Map<String, Object>>) response.getPayload().get("auctions");
                            
                            if (auctionDataList != null) {
                                for (Map<String, Object> auctionData : auctionDataList) {
                                    addLocalAuction(auctionData);
                                }
                                System.out.println("Đã đồng bộ " + auctionDataList.size() + " phiên đấu giá từ Server.");
                            }

                            // Sau khi đồng bộ xong, báo cho Controller (Login/Register) để tiếp tục luồng
                            if (responseHandler != null) {
                                Platform.runLater(() -> responseHandler.accept(response));
                            }
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
    private static final Map<String, ItemFactory> factoryRegister = Map.of(
        "Art", new ArtFactory(),
        "Vehicle", new VehicleFactory(),
        "Electronics", new ElectronicsFactory()
    );
    /**
     * Tái tạo đối tượng Auction từ dữ liệu Map (payload) và thêm vào AuctionManager của Client.
     * Dùng chung cho cả PUSH (broadcast) và PULL (get all).
     */
    private void addLocalAuction(Map<String, Object> payload) {
        String auctionId = String.valueOf(payload.get("auctionId"));
        String itemId = String.valueOf(payload.get("itemId"));
        String sellerId = String.valueOf(payload.get("sellerId"));
        String sellerName = String.valueOf(payload.get("sellerName"));
        String name = String.valueOf(payload.get("name"));
        double startPrice = Double.parseDouble(String.valueOf(payload.get("startPrice")));
        String category = String.valueOf(payload.get("category"));
        String desc = String.valueOf(payload.get("description"));
        
        LocalDateTime startT = LocalDateTime.now();
        if (payload.get("startTime") != null) {
            startT = LocalDateTime.parse(String.valueOf(payload.get("startTime")));
        }
        LocalDateTime endT = LocalDateTime.parse(String.valueOf(payload.get("endTime")));
        //factory cho item
        ItemFactory factory = factoryRegister.get(category);
        if (factory == null) {
            throw new IllegalArgumentException("Danh mục không hợp lệ: " + category);
        }
        Item localItem = factory.createItem(name, desc);
        localItem.setId(itemId);
        //tạo 1 local đối tượng auction
        NormalUser baseUser = new NormalUser(sellerName, "");
        baseUser.setId(sellerId);
        Auction localAuction = new Auction(localItem, new Seller(baseUser), startPrice, startT, endT);
        localAuction.setId(auctionId);

        if (payload.get("status") != null) {
            localAuction.setStatus(AuctionStatus.valueOf(String.valueOf(payload.get("status"))));
        }

        if (payload.get("highestBidderId") != null) {
            localAuction.setHighestBidderId(String.valueOf(payload.get("highestBidderId")));
        }

        // Phục hồi lịch sử đấu giá từ Server về Client
        if (payload.get("bidHistory") != null) {
            List<Map<String, Object>> historyList = (List<Map<String, Object>>) payload.get("bidHistory");
            for (Map<String, Object> bidMap : historyList) {
                String bId = String.valueOf(bidMap.get("bidderId"));
                String bidName = String.valueOf(bidMap.get("bidderName"));
                double amt = Double.parseDouble(String.valueOf(bidMap.get("amount")));
                LocalDateTime ts = LocalDateTime.parse(String.valueOf(bidMap.get("timestamp")));
                localAuction.addBidToHistory(new BidTransaction(auctionId, bId, bidName, amt, ts));
            }
        }

        //nhét vào RAM của Client
        AuctionManager.getINSTANCE().addAuction(localAuction);
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

    public void setUser(String userId, String userName, double balance) {
        this.userId = userId;
        this.userName = userName;
        this.totalBalance = balance;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void clearUser() {
        this.userId = null;
        this.userName = null;
        this.totalBalance = 0;
    }


}
