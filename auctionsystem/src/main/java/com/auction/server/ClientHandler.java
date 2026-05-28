package com.auction.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.auction.model.auction.AuctionStatus;
import com.auction.network.message.Request;
import com.auction.network.message.Response;
import com.auction.service.UserManager;
import com.auction.service.AuctionManager;
import com.auction.model.user.NormalUser;
import com.auction.model.user.Seller;
import com.auction.model.item.Item;
import com.auction.model.item.ItemFactory;
import com.auction.model.item.Art;
import com.auction.model.item.ArtFactory;
import com.auction.model.item.Electronics;
import com.auction.model.item.ElectronicsFactory;
import com.auction.model.item.Vehicle;
import com.auction.model.item.VehicleFactory;
import com.auction.model.auction.Auction;
import com.auction.model.auction.BidTransaction;
import com.auction.util.PersistenceService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

import com.google.gson.Gson;

public class ClientHandler implements Runnable {
    private final Socket socket; // final vì không bao giờ đổi kết nối sau khi tạo Handler
    private BufferedReader reader;
    private PrintWriter writer;
    private final Gson gson;     // final giúp bảo vệ instance Gson
    private NormalUser user;
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Lỗi thiết lập stream: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String jsonMessage;
            // Liên tục lắng nghe tin nhắn từ Client này
            while ((jsonMessage = reader.readLine()) != null) {
                System.out.println("\n[Server nhận]: " + jsonMessage);
                
                Request request = gson.fromJson(jsonMessage, Request.class);
                
                Response response = handleRequest(request);
                
                sendResponse(response);
            }
        } catch (IOException e) {
            System.out.println("Client đã ngắt kết nối: " + socket.getInetAddress());
        } finally {
            closeEverything();
        }
    }

    // phân loại command và gọi logic của server
    private Response handleRequest(Request request) {
        String command = request.getCommand();
        Response response = new Response();
        response.setCommand(command + "_RES"); // VD: Nhận LOGIN thì trả về LOGIN_RES

        try {
            if ("LOGIN".equals(command)) {
                String username = (String) request.getPayload().get("username");
                String password = (String) request.getPayload().get("password");
                
                // Nối thẳng vào hàm login của UserManager đã có sẵn!
                this.user = UserManager.getINSTANCE().login(username, password);
                
                response.setStatus("SUCCESS");
                response.setMessage("Đăng nhập thành công!");
                response.addData("userId", user.getId());
                response.addData("username", user.getName());
                response.addData("balance", user.getBalance());
            } else if ("REGISTER".equals(command)) {
                String regUsername = (String) request.getPayload().get("username");
                String regPassword = (String) request.getPayload().get("password");
                
                this.user = UserManager.getINSTANCE().register(regUsername, regPassword);
                
                response.setStatus("SUCCESS");
                response.setMessage("Đăng ký thành công!");
                response.addData("userId", user.getId());
                response.addData("username", user.getName());
                // Lưu dữ liệu sau khi đăng ký thành công
            
                PersistenceService.saveUser(user);

            } else if ("GET_ALL_AUCTIONS".equals(command)) {
                List<Auction> allAuctions = AuctionManager.getINSTANCE().getAllAuctions();
                
                // Chuyển đổi List<Auction> thành List<Map> để Gson không bị lỗi đa hình
                List<Map<String, Object>> auctionDataList = new ArrayList<>();
                for (Auction auction : allAuctions) {
                    Map<String, Object> auctionData = new HashMap<>();
                    auctionData.put("auctionId", auction.getId());
                    auctionData.put("itemId", auction.getItem().getId());
                    auctionData.put("sellerId", auction.getSeller().getId());
                    auctionData.put("sellerName", auction.getSeller().getName());
                    auctionData.put("name", auction.getItem().getName());
                    auctionData.put("startPrice", auction.getHighestBid()); // Giá hiện tại
                    auctionData.put("category", auction.getItem().getCategory());

                    auctionData.put("description", auction.getItem().getDescription());
                    auctionData.put("startTime", auction.getStartTime().toString());
                    auctionData.put("endTime", auction.getEndTime().toString());
                    auctionData.put("status", auction.getStatus().name());
                    
                    if (auction.getHighestBidderId() != null) {
                        auctionData.put("highestBidderId", auction.getHighestBidderId());
                    }
                    
                    // Thêm dữ liệu lịch sử đặt giá vào gói tin
                    List<Map<String, Object>> historyData = new ArrayList<>();
                    for (BidTransaction bid : auction.getBidHistory()) {
                        Map<String, Object> bidMap = new HashMap<>();
                        bidMap.put("bidderId", bid.getBidderId());
                        bidMap.put("bidderName", bid.getBidderName());
                        bidMap.put("amount", bid.getAmount());
                        bidMap.put("timestamp", bid.getTimestamp().toString());
                        historyData.add(bidMap);
                    }
                    auctionData.put("bidHistory", historyData);

                    auctionDataList.add(auctionData);
                }

                response.setStatus("SUCCESS");
                response.addData("auctions", auctionDataList);
                
            } else if ("PLACE_BID".equals(command)) {
                String auctionId = (String) request.getPayload().get("auctionId");
                String bidderId = (String) request.getPayload().get("bidderId");
                double amount = (Double) request.getPayload().get("amount");
                
                AuctionManager.getINSTANCE().placeBid(auctionId, bidderId, amount);
                Auction auction = AuctionManager.getINSTANCE().getAuction(auctionId);
                NormalUser bidder = UserManager.getINSTANCE().getUserById(bidderId);
                String bidderName = bidder.getName();

                response.setStatus("SUCCESS");
                response.setMessage("Đặt giá thành công!");
                
                // Tối ưu: Lưu ngay lập tức lượt đặt giá mới và cập nhật trạng thái Auction
                PersistenceService.saveUser(bidder);
                PersistenceService.saveAuction(auction); // Cập nhật Metadata (highest_bid, highest_bidder_id)
                
                // Lấy lượt bid cuối cùng trong lịch sử để lưu riêng lẻ
                List<BidTransaction> history = auction.getBidHistory();
                if (!history.isEmpty()) {
                    PersistenceService.saveBid(history.get(history.size() - 1));
                }
                
                // Broadcast giá mới cho toàn bộ các Client đang online để update UI Realtime
                Response broadcastRes = new Response();
                broadcastRes.setCommand("NEW_BID_BROADCAST");
                broadcastRes.setStatus("SUCCESS");
                broadcastRes.addData("auctionId", auctionId);
                broadcastRes.addData("bidderId", bidderId);
                broadcastRes.addData("bidderName", bidderName);
                broadcastRes.addData("amount", amount);
                AuctionServer.broadcast(broadcastRes);
            } else if ("CREATE_AUCTION".equals(command)) {
                String sellerId = (String) request.getPayload().get("sellerId");
                String name = (String) request.getPayload().get("name");
                double startPrice = Double.parseDouble(String.valueOf(request.getPayload().get("startPrice")));
                String category = (String) request.getPayload().get("category");
                String description = (String) request.getPayload().get("description");
                String endTimeStr = (String) request.getPayload().get("endTime");
                LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
                LocalDateTime startTime = LocalDateTime.now();
                
                // Lấy User gốc từ bộ nhớ Server dựa vào ID
                NormalUser baseUser = UserManager.getINSTANCE().getUserById(sellerId);
                if (baseUser == null) {
                    throw new IllegalArgumentException("Không tìm thấy thông tin người dùng hợp lệ để tạo phiên!");
                }
                Seller seller = UserManager.getINSTANCE().getSellerRole(baseUser);
                ItemFactory itemFactory= setItemFactory(category);
                Item item = itemFactory.createItem(name, description);
                
                
                Auction auction = AuctionManager.getINSTANCE().createAuction(item, seller, startPrice, startTime, endTime);
                response.setStatus("SUCCESS");
                response.setMessage("Tạo phiên đấu giá thành công!");
                response.addData("auctionId", auction.getId());
                response.addData("itemId", item.getId());

                //Ibroadcast response cho tất cả các clienthandler đang hoạt động
                Response broadcastRes = new Response();
                broadcastRes.setCommand("NEW_AUCTION_BROADCAST");
                broadcastRes.setStatus("SUCCESS");
                broadcastRes.addData("auctionId", auction.getId());
                broadcastRes.addData("itemId", item.getId());
                broadcastRes.addData("sellerId", seller.getId());
                broadcastRes.addData("sellerName", seller.getName());
                broadcastRes.addData("name", name);
                broadcastRes.addData("startPrice", startPrice);
                broadcastRes.addData("category", category);
                broadcastRes.addData("description", description);
                broadcastRes.addData("startTime", startTime.toString());
                broadcastRes.addData("endTime", endTimeStr);
                
                AuctionServer.broadcast(broadcastRes);

                // Tối ưu: Chỉ lưu auction mới tạo
                PersistenceService.saveAuction(auction);
            } else if ("CANCEL_AUCTION".equals(command)) {
                String auctionId = (String) request.getPayload().get("auctionId");
                String sellerId = (String) request.getPayload().get("sellerId");
                
                Auction auction = AuctionManager.getINSTANCE().getAuction(auctionId);
                if (auction == null) {
                    response.setStatus("ERROR");
                    response.setMessage("Phiên đấu giá không tồn tại!");
                } else {
                    boolean success = auction.cancelAuction(sellerId);
                    if (success) {
                        response.setStatus("SUCCESS");
                        response.setMessage("Đã hủy phiên đấu giá thành công!");
                        
                        // Lưu trạng thái CANCELED xuống database
                        PersistenceService.saveAuction(auction);
                        
                        // Broadcast thông báo hủy cho tất cả client
                        Response broadcastRes = new Response();
                        broadcastRes.setCommand("STATUS_UPDATE_BROADCAST");
                        broadcastRes.setStatus("SUCCESS");
                        broadcastRes.addData("auctionId", auctionId);
                        broadcastRes.addData("newStatus", AuctionStatus.CANCELED);
                        AuctionServer.broadcast(broadcastRes);
                    } else {
                        response.setStatus("ERROR");
                        response.setMessage("Không thể hủy. Bạn không phải người tạo hoặc phiên đã kết thúc.");
                    }
                }
            } else {
                response.setStatus("ERROR");
                response.setMessage("Lệnh không được hỗ trợ: " + command);
            }
        } catch (IllegalArgumentException e) {
            // Bắt chính xác lỗi "Sai mật khẩu", "Tài khoản không tồn tại" từ UserManager
            response.setStatus("ERROR");
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatus("ERROR");
            response.setMessage("Lỗi Server nội bộ: " + e.getMessage());
            e.printStackTrace(); // In lỗi ra console Server để dễ debug
        }
        return response;
    }

    void sendResponse(Response response) {
        String jsonResponse = gson.toJson(response);
        writer.println(jsonResponse);
        System.out.println("[Server gửi]: " + jsonResponse);
    }

    private void closeEverything() {
        AuctionServer.removeClient(this);
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static final Map<String, ItemFactory> factoryRegister = Map.of(
    "Art", new ArtFactory(),
    "Vehicle", new VehicleFactory(),
    "Electronics", new ElectronicsFactory()
    );
    private ItemFactory setItemFactory(String category){
        ItemFactory factory = factoryRegister.get(category);
        if (factory == null) {
            throw new IllegalArgumentException("Danh mục không hợp lệ: " + category);
        }
        return factory;
    }
}