package com.auction.controller;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionObserver;
import com.auction.network.ClientManager;
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
import javafx.scene.layout.VBox;

public class LotItemController implements AuctionObserver {

    @FXML
    private ImageView imgProduct;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblTitle;

    @FXML
    private Label txtPrice;

    @FXML
    private VBox lotCard; // container thẻ bên ngoài cùng

    @FXML
    private Label lblMyLot;

    private Auction auction;
    public void setData(Auction auction) {
        // nếu controller này đang theo dõi một auction khác, hãy hủy đăng ký trước
        cleanup();
        
        this.auction = auction;
        this.auction.addObserver(this); // đăng ký để nhận thông báo khi auction thay đổi
        
        updateUI();
    }

    // hàm dọn dẹp Observer để tránh rò rỉ bộ nhớ
    public void cleanup() {
        if (this.auction != null) {
            this.auction.removeObserver(this);
        }
    }

    @Override
    //observer
    public void update(Auction auction) {
        // cập nhật giao diện trên JavaFX Application Thread
        Platform.runLater(this::updateUI);
    }

    private void updateUI() {
        if (auction == null) return;
    
        lblStatus.setText(auction.getStatus().name());
        lblTitle.setText(auction.getItem().getName());

        // cập nhật giá dựa theo giá bid lớn nhất hiện tại
        txtPrice.setText(String.format("%.2f VND", auction.getHighestBid()));

        String currentUserId = ClientManager.getINSTANCE().getUserId();
        
        // 0. reset lại CSS cũ để tránh bị lưu dính style khi tái sử dụng thẻ (Recycle Node)
        if (lotCard != null) {
            lotCard.getStyleClass().remove("my-auction-card");
        }
        txtPrice.getStyleClass().remove("my-bid-price");
        if (txtPrice.getParent() != null) {
            txtPrice.getParent().setStyle(""); // xóa style nền cũ nếu có
        }

        // 1. highlight "Phiên của tôi tạo" (Viền nét đứt vàng)
        if (auction.getSeller().getId().equals(currentUserId)) {
            if (lotCard != null && !lotCard.getStyleClass().contains("my-auction-card")) {
                lotCard.getStyleClass().add("my-auction-card");
            }
            if (lblMyLot != null) {
                lblMyLot.setVisible(true);
                lblMyLot.setManaged(true);
            }
        } else {
            if (lblMyLot != null) {
                lblMyLot.setVisible(false);
                lblMyLot.setManaged(false);
            }
        }
        
        // 2. highlight "Phiên tôi đang bid" (Đổi nền HBox/VBox chứa giá sang màu xanh dương nhạt)
        boolean isParticipating = auction.getBidHistory().stream()
                .anyMatch(b -> b.getBidderId().equals(currentUserId));
        if (isParticipating) {
            txtPrice.getStyleClass().add("my-bid-price");
            if (txtPrice.getParent() != null) { // đổi nền của cả cái khung chứa chữ Current Price
                txtPrice.getParent().setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #90caf9; -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-padding: 3px;");
            }
        }
    }

    @FXML
    public void handleDetails(ActionEvent event) {
        System.out.println("Xem chi tiết phiên đấu giá: " + auction.getId());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/itemDetails.fxml"));
            Parent detailsView = loader.load();

            // lấy controller của màn hình chi tiết
            ItemDetailsController detailsController = loader.getController();
            
            detailsController.setData(this.auction);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(auction.getItem().getName());
            stage.setScene(new Scene(detailsView));
            
            // bắt sự kiện khi người dùng bấm nút "X" tắt cửa sổ để dọn dẹp
            stage.setOnHidden(e -> detailsController.cleanup());
            
            stage.show();
        } catch (Exception e) {
            // in toàn bộ lỗi ra để biết chính xác lỗi ở dòng nào, file nào
            e.printStackTrace();
        }
    }
}