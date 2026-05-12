package com.auction.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import com.auction.model.user.NormalUser;
import com.auction.network.ClientManager;
import java.io.IOException;

public class LoginController {
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private void handleTransferToRegister(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/register.fxml"));
            Parent registerView = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(registerView);
            stage.setScene(scene);
            stage.setTitle("Register");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.out.println("Error");
        }
    }
    @FXML
    private void handleLoginAction(ActionEvent actionEvent) {        
        boolean check = basicCheck();
        if (check) {
            //Controller kiểm tra kết quả do ClientManager ném sang
            ClientManager.getINSTANCE().setResponseHandler(response -> {
                //kiểm tra xem có đúng là LOGIN_RES không
                if ("LOGIN_RES".equals(response.getCommand())) {
                    if ("SUCCESS".equals(response.getStatus())) {
                        // Đăng nhập thành công
                        ClientManager.getINSTANCE().setCurrentUser(new NormalUser(txtUsername.getText(), txtPassword.getText()));
                        responseSuccess();
                    } else {
                        // Thất bại -> Lấy thông báo lỗi từ Server và hiển thị
                        showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", response.getMessage());
                    }
                }
            });

            //Yêu cầu ClientManager gửi dữ liệu đăng nhập đi
            ClientManager.getINSTANCE().login(txtUsername.getText(), txtPassword.getText());
        }
    }
    private void responseSuccess(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/mainPage.fxml"));
            Parent mainView = loader.load();
            // Sử dụng txtUsername để lấy Stage vì hàm này được gọi từ luồng ngầm, không có ActionEvent
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(mainView);
            stage.setScene(scene);
            stage.setTitle("Auction Client");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Login Error");
        }
    }

    private boolean basicCheck() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

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
