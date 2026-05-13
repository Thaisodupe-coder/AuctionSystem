package com.auction.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.auction.network.message.Request;
import com.auction.network.message.Response;
import com.auction.service.UserManager;
import com.auction.service.AuctionManager;
import com.auction.model.user.NormalUser;
import com.auction.model.user.Seller;
import com.auction.model.item.Item;
import com.auction.model.item.Art;
import com.auction.model.item.Electronics;
import com.auction.model.item.Vehicle;
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
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Gson gson;
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
                    
                    String category = "";
                    if (auction.getItem() instanceof Art) category = "Art";
                    else if (auction.getItem() instanceof Electronics) category = "Electronics";
                    else if (auction.getItem() instanceof Vehicle) category = "Vehicle";
                    auctionData.put("category", category);

                    auctionData.put("description", auction.getItem().getDescription());
                    auctionData.put("endTime", auction.getEndTime().toString());
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
                Item item;
                if ("Art".equals(category)) {
                    item = new Art(name, description);
                } else if ("Electronics".equals(category)) {
                    item = new Electronics(name, description);
                } else if ("Vehicle".equals(category)) {
                    item = new Vehicle(name, description);
                } else {
                    throw new IllegalArgumentException("Danh mục không hợp lệ");
                }
                
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
                broadcastRes.addData("endTime", endTimeStr);
                
                AuctionServer.broadcast(broadcastRes);

                // Tối ưu: Chỉ lưu auction mới tạo
                PersistenceService.saveAuction(auction);
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

    public void sendResponse(Response response) {
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
}