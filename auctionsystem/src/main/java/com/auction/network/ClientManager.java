package com.auction.network;

import com.auction.model.user.User;
import com.auction.network.message.Request;
import com.auction.network.message.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;
import javafx.application.Platform;

public class ClientManager {
    private volatile static ClientManager INSTANCE;
    private Gson gson = new Gson();
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Consumer<Response> responseHandler; // Callback để báo cho Controller biết có kết quả

    private User currentUser;

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
                    Response response = gson.fromJson(jsonResponse, Response.class);
                    
                    // Báo cho UI xử lý (Bắt buộc dùng Platform.runLater với JavaFX)
                    if (responseHandler != null) {
                        Platform.runLater(() -> responseHandler.accept(response));
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

    public void login(String username, String password){
        Request request = new Request("LOGIN");
        request.addData("username", username);
        request.addData("password", password);
        sendRequest(request);
    }

    public void register(String username, String password) {
        Request request = new Request("REGISTER");
        request.addData("username", username);
        request.addData("password", password);
        sendRequest(request);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void clearUser() {
        this.currentUser = null;
    }
}
