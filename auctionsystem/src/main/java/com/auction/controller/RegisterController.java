package com.auction.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import com.auction.network.ClientManager;
import com.auction.network.message.Request;

import java.io.IOException;

public class RegisterController {
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtPasswordCheck;

    @FXML
    private void handleRegisterAction(ActionEvent actionEvent) {
        boolean check = basicCheck();
        if (check) {
            ClientManager.getINSTANCE().setResponseHandler(response -> {
                if ("REGISTER_RES".equals(response.getCommand())) {
                    if ("SUCCESS".equals(response.getStatus())) {
                        String serverUserId = (String) response.getPayload().get("userId");
                        String serverUsername = txtUsername.getText();
                        ClientManager.getINSTANCE().setUser(serverUserId, serverUsername);
                        responseSuccess();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Đăng ký thất bại", response.getMessage());
                    }
                }
            });

            // Controller tự đóng gói Request và nhờ ClientManager gửi đi
            Request request = new Request("REGISTER");
            request.addData("username", txtUsername.getText());
            request.addData("password", txtPassword.getText());
            ClientManager.getINSTANCE().sendRequest(request);
        }
    }
    
    @FXML
    private void handleTransferToLogin(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/login.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.out.println("Login Error");
        }
    }
    
    private void responseSuccess() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/mainPage.fxml"));
            Parent mainView = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(mainView);
            stage.setScene(scene);
            stage.setTitle("Auction Client");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.out.println("Register Error");
        }
    }

    
    private boolean basicCheck() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String passwordCheck = txtPasswordCheck.getText();

        if (username == null || username.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi xác thực", "Tên tài khoản không được để trống!");
            return false;
        }
        if (password == null || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi xác thực", "Mật khẩu không được để trống!");
            return false;
        }
        if (password.contains(" ")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi xác thực", "Mật khẩu không được chứa khoảng trắng!");
            return false;
        }
        if (!password.equals(passwordCheck)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi xác thực", "Mật khẩu nhập lại không khớp!");
            return false;
        }
        
        return true;
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
