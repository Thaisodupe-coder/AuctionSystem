package com.auction.controller;

import com.auction.model.item.Item;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LotItemController {
    @FXML private Label lblTitle;
    @FXML
    private Label txtPrice;

    public void setData(Item item) {
        lblTitle.setText(item.getName());
        txtPrice.setText(String.format("%,.0f VND", item.getCurrentPrice()));
    }
}