package com.auction.controller;

import com.auction.model.auction.Auction;
import com.auction.model.auction.AuctionObserver;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.auction.BidTransaction;
import com.auction.network.ClientManager;
import com.auction.network.message.Request;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.NumberAxis;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.Cursor;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.stage.Stage;
import java.awt.Toolkit;
import java.util.Map.Entry;

public class ItemDetailsController implements AuctionObserver {
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter timeOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

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
    private Label lblWinner; // Tạo 1 label mới trong SceneBuilder và gắn fx:id="lblWinner" để hiện tên người thắng

    @FXML
    private TableView<BidDisplayItem> tvBidHistory;

    @FXML
    private LineChart<String, Number> lineChartBidHistory;

    @FXML
    private Button btnToggleView;

    // Model class nội bộ để hiển thị bảng
    public static class BidDisplayItem {
        private final StringProperty time;
        private final StringProperty user;
        private final StringProperty price;

        public BidDisplayItem(String time, String user, String price) {
            this.time = new SimpleStringProperty(time);
            this.user = new SimpleStringProperty(user);
            this.price = new SimpleStringProperty(price);
        }
        public String getTime() { return time.get(); }
        public String getUser() { return user.get(); }
        public String getPrice() { return price.get(); }
        public StringProperty timeProperty() { return time; }
        public StringProperty userProperty() { return user; }
        public StringProperty priceProperty() { return price; }
    }

    private final ObservableList<BidDisplayItem> bidLogItems = FXCollections.observableArrayList();
    private final XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();

    // Lưu các mốc cần vẽ vạch đỏ
    private final Map<String, LocalDateTime> markerData = new HashMap<>();

    private Auction auction;

    @FXML
    public void initialize() {
        if (tvBidHistory != null) {
            TableColumn<BidDisplayItem, String> timeCol = new TableColumn<>("THỜI GIAN");
            timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
            timeCol.setPrefWidth(170); 
            timeCol.getStyleClass().add("time-column");
            timeCol.setStyle("-fx-alignment: CENTER");

            TableColumn<BidDisplayItem, String> bidderCol = new TableColumn<>("NGƯỜI ĐẶT");
            bidderCol.setCellValueFactory(new PropertyValueFactory<>("user"));
            bidderCol.setPrefWidth(110);
            bidderCol.setStyle("-fx-alignment: CENTER");

            TableColumn<BidDisplayItem, String> priceCol = new TableColumn<>("GIÁ VND");
            priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
            priceCol.setPrefWidth(110);
            priceCol.getStyleClass().add("price-column");
            priceCol.setStyle("-fx-alignment: CENTER");

            tvBidHistory.getColumns().addAll(timeCol, bidderCol, priceCol);
            tvBidHistory.setItems(bidLogItems);
            tvBidHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }

        // Khởi tạo series cho biểu đồ
        if (lineChartBidHistory != null) {
            lineChartBidHistory.getData().add(priceSeries);

            lineChartBidHistory.setVerticalGridLinesVisible(false);

            CategoryAxis xAxis = (CategoryAxis) lineChartBidHistory.getXAxis();
            xAxis.setTickLabelRotation(0);
            
            // Giữ cho thang đo giá ổn định, không bị nhảy về 0 khi có giá trị lớn
            if (lineChartBidHistory.getYAxis() instanceof NumberAxis) {
                ((NumberAxis) lineChartBidHistory.getYAxis()).setForceZeroInRange(false);
            }
        
            lineChartBidHistory.boundsInLocalProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(this::redrawDayMarker);
            });
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
        priceSeries.getData().clear();
        markerData.clear();
        
        // Xóa các vạch kẻ ngày cũ trên giao diện
        removeDayMarkers();

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
        Toolkit.getDefaultToolkit().beep();
        // Khi Auction có thay đổi (ví dụ: giá tăng), hàm này sẽ được gọi từ luồng mạng
        Platform.runLater(this::updateUI);
    }

    private void updateUI() {
        if (auction == null) return;

        lblDetailTitle.setText(auction.getItem().getName());
        txtUID.setText(auction.getId());

        lblTimestart.setText(auction.getStartTime().format(timeFormatter));
        lblTimeEnd.setText(auction.getEndTime().format(timeFormatter));
        lblDetailDescription.setText(auction.getItem().getDescription());
        
        //Cập nhật giá dựa theo giá bid lớn nhất hiện tại
        lblDetailPrice.setText(String.format("%.2f VND", auction.getHighestBid()));

        // Cập nhật lịch sử đặt giá vào ListView
        List<BidTransaction> history = auction.getBidHistory();
        
        String currentUserId = ClientManager.getINSTANCE().getUserId();
        // Chỉ thêm những bid mới mà UI chưa có
        if (history.size() > bidLogItems.size()) { //Kiểm tra nếu tổng bid từ sv lớn hơn bid hiện có trên màn hình
            // Duyệt từ vị trí hiện tại của UI đến hết lịch sử mới
            for (int i = bidLogItems.size(); i < history.size(); i++) {
                BidTransaction bid = history.get(i);
                
                String timeStr = bid.getTimestamp().format(timeFormatter);
                String priceStr = String.format("%,.2f", bid.getAmount());
                
                String bidderName = (bid.getBidderName() != null && !bid.getBidderName().isEmpty()) 
                                    ? bid.getBidderName() : "Người đấu giá";

                // Thêm vào bảng dưới dạng Object thay vì String
                bidLogItems.add(0, new BidDisplayItem(timeStr, bidderName, priceStr));

                // Đồ thị: Trục hoành chỉ hiển thị thời gian
                LocalDate currentBidDate = bid.getTimestamp().toLocalDate();
                
                // Nếu là bid đầu tiên (i=0) hoặc khác ngày với bid phía trước
                boolean isFirstBidOfDay = (i == 0) || !currentBidDate.equals(history.get(i - 1).getTimestamp().toLocalDate());

                // Dùng khoảng trắng để CategoryAxis không gộp các bid trùng giây
                String uniqueLabel = bid.getTimestamp().format(timeOnlyFormatter) + "\u200B".repeat(i);
                priceSeries.getData().add(new XYChart.Data<>(uniqueLabel, bid.getAmount()));

                if (isFirstBidOfDay) {
                    markerData.putIfAbsent(uniqueLabel, bid.getTimestamp());
                }
            }
            Platform.runLater(this::redrawDayMarker);
        }

        // Thay đổi giao diện tùy thuộc vào trạng thái phiên đấu giá
        if (auction.getStatus() == AuctionStatus.FINISHED || 
            auction.getStatus() == AuctionStatus.PAID || 
            auction.getStatus() == AuctionStatus.CANCELED) {
            lblDetailCondition.setText("ĐÃ KẾT THÚC");
            lblDetailCondition.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white; -fx-padding: 3px 8px; -fx-background-radius: 5px;");
            
            txtBidInput.setDisable(true);
            if (btnPlaceBid != null) btnPlaceBid.setDisable(true);
            
            if (auction.getHighestBidderId() != null && !auction.getHighestBidderId().isEmpty()) {
                // Lấy tên nguời thắng cuộc, ưu tiên nếu là chính mình thì lấy tên từ ClientManager
                String winnerName;
                if (auction.getHighestBidderId().equals(currentUserId)) {
                    winnerName = ClientManager.getINSTANCE().getUserName();
                } else {
                    // Tìm trong lịch sử bid để lấy chính xác tên người thắng
                    winnerName = auction.getBidHistory().stream()
                            .filter(b -> b.getBidderId().equals(auction.getHighestBidderId()))
                            .map(BidTransaction::getBidderName)
                            .findFirst()
                            .orElse("Chưa xác định");
                }

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

    private void redrawDayMarker() { // Vạch kẻ động vẽ lại vạch mới bám sát theo dữ liệu mới nhất
        removeDayMarkers(); // Xóa vạch cũ để vẽ lại vạch mới cập nhật tọa độ
        if (!lineChartBidHistory.isVisible()) return;

        CategoryAxis xAxis = (CategoryAxis) lineChartBidHistory.getXAxis();

        // Lấy khung chứa đồ thị để vẽ lên
        Node chartBackground = lineChartBidHistory.lookup(".chart-plot-background");

        if (chartBackground == null) return;

        Pane parent = (Pane) lineChartBidHistory.getParent();

        for (Entry<String, LocalDateTime> entry : markerData.entrySet()) {
            String category = entry.getKey();
            LocalDateTime dateTime = entry.getValue();

            double xPos = xAxis.getDisplayPosition(category);

            // Chỉ vẽ nếu xPos hợp lệ và nằm bên trong khung của biểu đồ
            if (xPos >= 0 && xPos <= chartBackground.getBoundsInLocal().getWidth()) {
            
                Line line = new Line();
                line.setStroke(Color.web("#d9534f"));
                line.setStrokeWidth(2.0);
                line.getStrokeDashArray().addAll(5.0, 5.0); // Nét đứt
                line.getStyleClass().add("day-marker");
                line.setCursor(Cursor.HAND); // Đổi con trỏ chuột khi hover vào vạch
                
                // RÀNG BUỘC: Chỉ hiển thị vạch kẻ khi đồ thị đang hiển thị
                line.visibleProperty().bind(lineChartBidHistory.visibleProperty());

                // Tọa độ vạch kẻ (tương đối trong StackPane)
                line.setStartX(chartBackground.getLayoutX() + xPos);
                line.setEndX(chartBackground.getLayoutX() + xPos);
                line.setStartY(chartBackground.getLayoutY());
                line.setEndY(chartBackground.getLayoutY() + chartBackground.getBoundsInLocal().getHeight());

                // Hiển thị đầy đủ ngày tháng năm và thời gian khi hover
                Tooltip tooltip = new Tooltip("Mốc thời gian: " + dateTime.format(timeFormatter));
                tooltip.setShowDelay(javafx.util.Duration.millis(100)); // Hiện tooltip nhanh hơn
                Tooltip.install(line, tooltip);

                parent.getChildren().add(line);
            }
        }
    }

    private void removeDayMarkers() {
        if (lineChartBidHistory != null && lineChartBidHistory.getParent() instanceof Pane) {
            Pane parent = (Pane) lineChartBidHistory.getParent();
            parent.getChildren().removeIf(node -> node.getStyleClass().contains("day-marker"));
        }
    }

    @FXML
    public void handleToggleView() {
        boolean isListVisible = tvBidHistory.visibleProperty().get();
        
        if (isListVisible) {
            tvBidHistory.setVisible(false);
            lineChartBidHistory.setVisible(true);
            // Vẽ lại vạch đỏ ngay khi hiện đồ thị
            Platform.runLater(this::redrawDayMarker);
            btnToggleView.setText("XEM LỊCH SỬ ĐẶT GIÁ");
        } else {
            tvBidHistory.setVisible(true);
            lineChartBidHistory.setVisible(false);
            // Xóa vạch đỏ khi chuyển sang danh sách
            removeDayMarkers();
            btnToggleView.setText("XEM ĐỒ THỊ GIÁ");
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