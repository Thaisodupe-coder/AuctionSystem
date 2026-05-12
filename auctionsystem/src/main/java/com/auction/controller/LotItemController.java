package com.auction.controller;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionObserver;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Modality;

public class LotItemController implements AuctionObserver {

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
        // Nếu controller này đang theo dõi một auction khác, hãy hủy đăng ký trước
        if (this.auction != null) {
            this.auction.removeObserver(this);
        }
        
        this.auction = auction;
        this.auction.addObserver(this); // Đăng ký để nhận thông báo khi auction thay đổi
        
        updateUI();
    }

    @Override
    public void update(Auction auction) {
        // Cập nhật giao diện trên JavaFX Application Thread
        Platform.runLater(this::updateUI);
    }

    private void updateUI() {
        if (auction == null) return;
    
        lblStatus.setText(auction.getStatus().name());
        lblTitle.setText(auction.getItem().getName());

        //Cập nhật giá dựa theo giá bid lớn nhất hiện tại
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
            stage.setTitle(auction.getItem().getName());
            stage.setScene(new Scene(detailsView));
            stage.show();
        } catch (Exception e) {
            // In toàn bộ lỗi ra để biết chính xác lỗi ở dòng nào, file nào
            e.printStackTrace();
        }
    }
}