package com.auction.controller;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.auction.BidTransaction;
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

public class SellerViewController {

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
    private TableColumn<Auction, String> colStatus;

    @FXML
    private TableColumn<Auction, String> colBuyer;

    @FXML
    public void initialize() {
        // 1. Thiết lập cách lấy dữ liệu cho từng cột
        colId.setCellValueFactory(cellData -> {
            int index = soldItemsTable.getItems().indexOf(cellData.getValue());
            return new SimpleStringProperty(String.valueOf("#" + (index + 1)));
        });
        colId.setStyle("-fx-alignment: CENTER");

        colItemName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItem().getName()));
        colItemName.setStyle("-fx-alignment: CENTER");

        colPrice.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getHighestBid()).asObject());
        colPrice.getStyleClass().add("price-column");
        colPrice.setStyle("-fx-alignment: CENTER");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colSoldDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndTime().format(formatter)));
        colSoldDate.getStyleClass().add("time-column");
        colSoldDate.setStyle("-fx-alignment: CENTER");

        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().name()));
        colStatus.setStyle("-fx-alignment: CENTER");

        colBuyer.setCellValueFactory(cellData -> {
            Auction auction = cellData.getValue();
            AuctionStatus status = auction.getStatus();
            List<BidTransaction> history = auction.getBidHistory();
            String winnerId = auction.getHighestBidderId();

            String displayText;
            if (status == AuctionStatus.OPEN) {
                displayText = "Chưa bắt đầu";
            } else if (status == AuctionStatus.CANCELED) {
                displayText = "---";
            } else if (winnerId != null && !history.isEmpty()) {
                String winnerName = history.get(history.size() - 1).getBidderName();
                displayText = (status == AuctionStatus.FINISHED || status == AuctionStatus.PAID) ? "🏆 " + winnerName : winnerName;
            } else {
                displayText = (status == AuctionStatus.FINISHED || status == AuctionStatus.PAID) ? "Không có người mua" : "Chưa có lượt đặt";
            }
            return new SimpleStringProperty(displayText);
        });
        colBuyer.setStyle("-fx-alignment: CENTER");

        // 2. Lấy dữ liệu của User hiện tại và đưa vào bảng
        String currentUserId = ClientManager.getINSTANCE().getUserId();
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
