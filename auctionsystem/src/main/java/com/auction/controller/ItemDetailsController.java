package com.auction.controller;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionObserver;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.user.NormalUser;
import com.auction.service.UserManager;
import com.auction.network.ClientManager;
import com.auction.network.message.Request;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @FXML
    private ListView<String> lvBidHistory;

    // Khai báo danh sách để lưu trữ các dòng log đấu giá
    private final ObservableList<String> bidLogItems = FXCollections.observableArrayList();

    private Auction auction;

    @FXML
    public void initialize() {
        // Kết nối danh sách dữ liệu với giao diện ListView
        if (lvBidHistory != null) {
            lvBidHistory.setItems(bidLogItems);
        }

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
        
        // Xóa lịch sử cũ của sản phẩm trước đó để không bị lẫn dữ liệu
        bidLogItems.clear();

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

        // Cập nhật lịch sử đặt giá vào ListView
        List<com.auction.model.auction.BidTransaction> history = auction.getBidHistory();
        
        // Chỉ thêm những bid mới mà UI chưa có
        if (history.size() > bidLogItems.size()) { //Kiểm tra nếu tổng bid từ sv lớn hơn bid hiện có trên màn hình
            // Duyệt từ vị trí hiện tại của UI đến hết lịch sử mới
            for (int i = bidLogItems.size(); i < history.size(); i++) {
                com.auction.model.auction.BidTransaction bid = history.get(i);
                
                // Tìm thông tin người dùng từ UserManager để lấy tên
                NormalUser bidder = UserManager.getINSTANCE().getUserById(bid.getBidderId());
                String bidderName = (bidder != null) ? bidder.getName() : "Người dùng " + bid.getBidderId();

                String log = String.format("[%s] %s: %.2f VND",
                        bid.getTimestamp().format(timeFormatter),
                        bidderName,
                        bid.getAmount());
                
                // Thêm vào vị trí 0 (đầu danh sách) để bid mới nhất luôn ở trên cùng
                bidLogItems.add(0, log);
            }
        }

        // Thay đổi giao diện tùy thuộc vào trạng thái phiên đấu giá
        if (auction.getStatus() == AuctionStatus.FINISHED) {
            lblDetailCondition.setText("ĐÃ KẾT THÚC");
            lblDetailCondition.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-padding: 3px 8px; -fx-background-radius: 5px;");
            
            txtBidInput.setDisable(true);
            if (btnPlaceBid != null) btnPlaceBid.setDisable(true);
            
            if (auction.getHighestBidderId() != null && !auction.getHighestBidderId().isEmpty()) {
                // Lấy tên người thắng thay vì ID
                NormalUser winner = UserManager.getINSTANCE().getUserById(auction.getHighestBidderId());
                String winnerName = (winner != null) ? winner.getName() : auction.getHighestBidderId();

                lblDetailPrice.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 3px 8px;"); // Nền xanh lá nhạt
                if (lblWinner != null) {
                    lblWinner.setText("🏆 WINNER: " + winnerName);
                    lblWinner.setStyle("-fx-text-fill: #155724; -fx-font-weight: bold;");
                    lblWinner.setVisible(true);
                } else {
                    lblDetailTitle.setText(auction.getItem().getName() + " - Winner: " + winnerName);
                }
                lblDetailPrice.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 5px; -fx-background-radius: 5px;");
            } else {
                if (lblWinner != null) {
                    lblWinner.setText("❌ Phiên đấu giá kết thúc (Không có người mua)");
                    lblWinner.setStyle("-fx-text-fill: #721c24;");
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