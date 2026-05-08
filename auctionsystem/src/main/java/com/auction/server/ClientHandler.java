package com.auction.server;

import com.auction.network.message.Request;
import com.auction.network.message.Response;
import com.auction.service.UserManager;
import com.auction.service.AuctionManager;
import com.auction.model.user.NormalUser;
import com.auction.util.PersistenceService;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
                PersistenceService.saveData();
            } else if ("PLACE_BID".equals(command)) {
                String auctionId = (String) request.getPayload().get("auctionId");
                String bidderId = (String) request.getPayload().get("bidderId");
            
                double amount = (Double) request.getPayload().get("amount");
                
                AuctionManager.getINSTANCE().placeBid(auctionId, bidderId, amount);
                response.setStatus("SUCCESS");
                response.setMessage("Đặt giá thành công!");
                // Lưu dữ liệu sau khi đặt giá thành công
                PersistenceService.saveData();
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
        }
        return response;
    }

    public void sendResponse(Response response) {
        String jsonResponse = gson.toJson(response);
        // PrintWriter tự động thêm dấu xuống dòng và flush dữ liệu đi
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