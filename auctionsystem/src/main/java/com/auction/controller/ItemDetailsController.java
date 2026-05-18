package com.auction.controller;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionObserver;
import com.auction.model.auction.AuctionStatus;
import com.auction.network.ClientManager;
import com.auction.network.message.Request;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import java.time.format.DateTimeFormatter;
import javafx.stage.Stage;
import java.awt.Toolkit;

public class ItemDetailsController implements AuctionObserver {
    @FXML
    private Label lblDetailTitle;
    @FXML
    private Label txtUID;

    @FXML
    private ImageView imgDetail;

    @FXML
    private Label lblDetailPrice;

    @FXML
    private Label lblDetailCondition;

    @FXML
    private Label lblTimestart;

    @FXML
    private Label lblTimeEnd;

    @FXML
    private TextField txtBidInput;
    @FXML
    private Label lblDetailDescription;

    @FXML
    private javafx.scene.control.Button btnPlaceBid; // Gắn fx:id="btnPlaceBid" cho nút "PLACE BID" trong SceneBuilder

    @FXML
    private Label lblWinner; // Tạo 1 Label mới trong SceneBuilder và gắn fx:id="lblWinner" để hiện tên người thắng

    private Auction auction;

    @FXML
    public void initialize() {
        // Chỉ cho phép nhập số nguyên (chỉ chấp nhận các ký tự từ 0-9)
        txtBidInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtBidInput.setText(oldValue);
            }
        });
    }

    public void setData(Auction auction) {
        // Nếu đang theo dõi auction cũ, hủy đăng ký trước khi nhận auction mới
        cleanup();
        
        //controller sẽ đăng kí theo dõi 1 auction (observer)
        this.auction = auction;
        this.auction.addObserver(this);
        updateUI();
    }

    // Hàm dọn dẹp Observer để tránh rò rỉ bộ nhớ
    public void cleanup() {
        if (this.auction != null) {
            this.auction.removeObserver(this);
        }
    }

    @Override
    public void update(Auction auction) {
        //sound
        Toolkit.getDefaultToolkit().beep();;
        // Khi Auction có thay đổi (ví dụ: giá tăng), hàm này sẽ được gọi từ luồng mạng
        Platform.runLater(this::updateUI);
    }

    private void updateUI() {
        if (auction == null) return;

        lblDetailTitle.setText(auction.getItem().getName());
        txtUID.setText(auction.getId());
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        lblTimestart.setText(auction.getStartTime().format(timeFormatter));
        lblTimeEnd.setText(auction.getEndTime().format(timeFormatter));
        lblDetailDescription.setText(auction.getItem().getDescription());
        
        //Cập nhật giá dựa theo giá bid lớn nhất hiện tại
        lblDetailPrice.setText(String.format("%.2f VND", auction.getHighestBid()));

        // Thay đổi giao diện tùy thuộc vào trạng thái phiên đấu giá
        if (auction.getStatus() == AuctionStatus.FINISHED) {
            lblDetailCondition.setText("ĐÃ KẾT THÚC");
            lblDetailCondition.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-padding: 3px 8px; -fx-background-radius: 5px;");
            
            txtBidInput.setDisable(true);
            if (btnPlaceBid != null) btnPlaceBid.setDisable(true);
            
            if (auction.getHighestBidderId() != null && !auction.getHighestBidderId().isEmpty()) {
                lblDetailPrice.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 3px 8px;"); // Nền xanh lá nhạt
                if (lblWinner != null) {
                    lblWinner.setText("Winner: " + auction.getHighestBidderId());
                    lblWinner.setVisible(true);
                } else {
                    lblDetailTitle.setText(auction.getItem().getName() + " - Winner: " + auction.getHighestBidderId());
                }
            } else {
                if (lblWinner != null) {
                    lblWinner.setText("Phiên đấu giá thất bại (Không có người mua)");
                    lblWinner.setVisible(true);
                } else {
                    lblDetailTitle.setText(auction.getItem().getName() + " - Thất bại");
                }
            }
        } else { // Trạng thái OPEN hoặc RUNNING
            lblDetailCondition.setText(auction.getStatus().name());
            lblDetailCondition.setStyle(""); // Đặt lại style mặc định
            
            // Kiểm tra nếu user hiện tại là người tạo phiên đấu giá thì vô hiệu hóa nút đặt giá
            if (auction.getSeller().getId().equals(ClientManager.getINSTANCE().getUserId())) {
                txtBidInput.setDisable(true);
                txtBidInput.setPromptText("Sản phẩm của bạn");
                if (btnPlaceBid != null) btnPlaceBid.setDisable(true);
            } else {
                txtBidInput.setDisable(false);
                txtBidInput.setPromptText("Enter amount...");
                if (btnPlaceBid != null) btnPlaceBid.setDisable(false);
            }
            lblDetailPrice.setStyle("");
            if (lblWinner != null) lblWinner.setVisible(false);
        }
    }

    @FXML
    public void handleBackToMain(ActionEvent event) {
        cleanup(); // Dọn dẹp trước khi đóng bằng nút Back
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handlePlaceBid(ActionEvent event) {
        try {
            String input = txtBidInput.getText();
            if (input == null || input.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng nhập giá tiền muốn đặt!");
                return;
            }

            double amount = Double.parseDouble(input);

            //Kiểm tra trạng thái phiên đấu giá
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                showAlert(Alert.AlertType.ERROR, "Lỗi đặt giá", "Chỉ có thể đặt giá khi phiên đấu giá đang diễn ra!");
                return;
            }

            if (amount <= auction.getHighestBid()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi đặt giá", "Giá đặt phải cao hơn giá hiện tại!");
                return;
            }
            
            // Đăng ký nhận phản hồi từ Server để cập nhật UI Realtime
            ClientManager.getINSTANCE().setResponseHandler(response -> {
                if ("PLACE_BID_RES".equals(response.getCommand())) {
                    Platform.runLater(() -> {
                        if ("SUCCESS".equals(response.getStatus())) {
                            // Chỉ cần xóa ô nhập khi có phản hồi SUCCESS.
                            // Việc cập nhật giá và UI sẽ được lắng nghe thông qua NEW_BID_BROADCAST trong ClientManager.
                            txtBidInput.clear();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Đặt giá thất bại", response.getMessage());
                        }
                    });
                }
            });

            // Gửi Request lên Server
            Request request = new Request("PLACE_BID");
            request.addData("auctionId", auction.getId());
            request.addData("bidderId", ClientManager.getINSTANCE().getUserId());
            request.addData("amount", amount);
            
            ClientManager.getINSTANCE().sendRequest(request);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng nhập số tiền hợp lệ!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}