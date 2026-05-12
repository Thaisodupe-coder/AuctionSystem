package com.auction.controller;

import com.auction.model.auction.Auction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;

public class LotItemController {

    @FXML
    private ImageView imgProduct;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTitle;

    @FXML
    private Label txtPrice;

    private Auction auction;

    public void setData(Auction auction) {
        this.auction = auction;
        
        lblStatus.setText(auction.getStatus().name());
        lblTitle.setText(auction.getItem().getName()); 
        txtPrice.setText(String.format("%.2f VND", auction.getHighestBid()));
    }

    @FXML
    public void handleDetails(ActionEvent event) {
        System.out.println("Xem chi tiết phiên đấu giá: " + auction.getId());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/itemDetails.fxml"));
            Parent detailsView = loader.load();

            // Lấy controller của màn hình chi tiết
            ItemDetailsController detailsController = loader.getController();
            
            detailsController.setData(this.auction);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi tiết: " + auction.getItem().getName());
            stage.setScene(new Scene(detailsView));
            stage.show();
        } catch (Exception e) {
            // In toàn bộ lỗi ra để biết chính xác lỗi ở dòng nào, file nào
            e.printStackTrace();
        }
    }
}