package com.auction.controller;
//sửa cũng được
import com.auction.model.item.Item;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainPageController {
    @FXML
    private FlowPane gridPaneAuctions;
    @FXML
    public void initialize() {
        List<Item> items = new ArrayList<>();
        renderItems(items);
    }
    private void renderItems(List<Item> items) {
        gridPaneAuctions.getChildren().clear();

        try {
            for (Item item : items) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/com/auction/client/view/itemView.fxml"));

                VBox itemNode = loader.load();

                //LotItemController controller = loader.getController();
                //controller.setData(item);

                gridPaneAuctions.getChildren().add(itemNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void handleLogoutAction(ActionEvent actionEvent) {
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
            e.printStackTrace();
            System.out.println("Cant logout");
        }
    }
}
