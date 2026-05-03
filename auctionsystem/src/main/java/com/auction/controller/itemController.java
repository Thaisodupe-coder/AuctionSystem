package com.auction.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.auction.model.item.Item;
import java.io.IOException;

public class itemController {
    @FXML private Label lblTitle;
    @FXML private Label txtPrice;
    private Item myItem;
    public void setData(Item item) {
        this.myItem = item;
        lblTitle.setText(item.getName());
        txtPrice.setText(item.getCurrentPrice() + " VND");
    }
    @FXML
    public void handleViewDetails(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/itemDetails.fxml"));
            Parent detailView = loader.load();
            itemDetailsController detailController = loader.getController();
            detailController.setItemData(this.myItem);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(detailView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}