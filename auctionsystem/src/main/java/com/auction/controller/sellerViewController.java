package com.auction.controller;

import com.auction.model.auction.Auction;
import com.auction.network.ClientManager;
import com.auction.service.AuctionManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class sellerViewController {

    @FXML
    private TableView<Auction> soldItemsTable;

    @FXML
    private TableColumn<Auction, String> colId;

    @FXML
    private TableColumn<Auction, String> colItemName;

    @FXML
    private TableColumn<Auction, Double> colPrice;

    @FXML
    private TableColumn<Auction, String> colSoldDate;

    @FXML
    private TableColumn<Auction, String> colBuyer;

    @FXML
    public void initialize() {
        // 1. Thiết lập cách lấy dữ liệu cho từng cột
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colPrice.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getHighestBid()).asObject());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colSoldDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndTime().format(formatter)));

        colBuyer.setCellValueFactory(cellData -> {
            String buyerId = cellData.getValue().getHighestBidderId();
            return new SimpleStringProperty(buyerId != null ? buyerId : "Chưa có");
        });

        // 2. Lấy dữ liệu của User hiện tại và đưa vào bảng
        String currentUserId = ClientManager.getINSTANCE().getCurrentUser().getId();
        List<Auction> myAuctions = AuctionManager.getINSTANCE().getAuctionsBySeller(currentUserId);
        
        ObservableList<Auction> observableAuctions = FXCollections.observableArrayList(myAuctions);
        soldItemsTable.setItems(observableAuctions);
    }

    @FXML
    public void handleBackToMain(ActionEvent actionEvent) {
        // Đóng cửa sổ Inventory hiện tại (overlay) để quay lại MainPage ở dưới
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }
}