package com.auction.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import com.auction.model.item.Item;
import javafx.stage.Stage;
import java.io.IOException;

public class itemDetailsController {

    @FXML private Label lblDetailTitle;
    @FXML private Label lblDetailPrice;
    @FXML private Label lblDetailDescription;
    private Item currentItem;
    public void setItemData(Item item) {
        this.currentItem = item;
        lblDetailTitle.setText(item.getName());
        lblDetailPrice.setText(String.valueOf(item.getCurrentPrice()) + " VND");
        lblDetailDescription.setText(item.getDescription());
    }
    public void handleBackToMain(javafx.event.ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/mainPage.fxml"));
            Parent mainView = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(mainView);
            stage.setScene(scene);
            stage.setTitle("Auction Client");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error");
        }
    }
}