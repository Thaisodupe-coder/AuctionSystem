package com.auction.controller;

import com.auction.model.auction.Auction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

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
    }
}