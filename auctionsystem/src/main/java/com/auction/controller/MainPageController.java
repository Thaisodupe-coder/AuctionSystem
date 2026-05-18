package com.auction.controller;
//sửa cũng được
import com.auction.model.auction.Auction;
import com.auction.model.item.Item;
import com.auction.service.AuctionManager;
import com.auction.network.ClientManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToggleButton;
import com.auction.model.auction.AuctionStatus;
import com.auction.model.item.Art;
import com.auction.model.item.Electronics;
import com.auction.model.item.Vehicle;

public class MainPageController {
    @FXML
    private FlowPane gridPaneAuctions;
    @FXML
    private Button txtusename;

    @FXML
    private ToggleGroup filterGroup;

    @FXML
    private ComboBox<String> cbCategory;

    private boolean isInitialized = false;

    @FXML
    public void initialize() {
        String username = ClientManager.getINSTANCE().getUserName();
        String userId = ClientManager.getINSTANCE().getUserId();
        double balance = 100;//ClientManager.getINSTANCE().getCurrentUser().getBalance();
        int myAuctionCount = AuctionManager.getINSTANCE().getAuctionsBySeller(userId).size();

        if (username != null) {
            txtusename.setText("👤 " + username + "   |   💰 " + balance + "   |   📦 Lots: " + myAuctionCount);
        }

        if (!isInitialized) {
            // Cài đặt danh sách đổ xuống cho Categories
            if (cbCategory != null) {
                cbCategory.getItems().addAll("All", "Art", "Electronics", "Vehicle");
                cbCategory.setValue("All");
                cbCategory.setOnAction(e -> applyFilters());
            }

            // Cài đặt sự kiện lắng nghe khi ấn vào các nút phân loại
            if (filterGroup != null) {
                filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal == null) {
                        oldVal.setSelected(true); // Ngăn người dùng bỏ chọn tất cả
                        return;
                    }
                    applyFilters();
                });
            }
            isInitialized = true;
        }
        
        applyFilters(); // Bắt đầu lọc dữ liệu dựa trên trạng thái mặc định
    }

    private void applyFilters() {
        List<Auction> allAuctions = AuctionManager.getINSTANCE().getAllAuctions();
        String currentUserId = ClientManager.getINSTANCE().getUserId();
        
        String tempFilter = "All Lots";
        if (filterGroup != null && filterGroup.getSelectedToggle() != null) {
            tempFilter = ((ToggleButton) filterGroup.getSelectedToggle()).getText();
        }
        final String filterType = tempFilter;

        // Lọc theo nút (Active / My Lots / My Bids)
        List<Auction> filtered = allAuctions.stream().filter(auction -> {
            if ("Active Lots".equals(filterType)) {
                return auction.getStatus() == AuctionStatus.RUNNING;
            } else if ("My Lots".equals(filterType)) {
                return currentUserId != null && auction.getSeller().getId().equals(currentUserId);
            } else if ("My Bids".equals(filterType)) {
                return currentUserId != null && auction.getBidHistory().stream().anyMatch(b -> b.getBidderId().equals(currentUserId));
            } else {
                return true;
            }
        }).collect(Collectors.toList());

        // Lọc chồng thêm theo danh mục sản phẩm (Categories)
        String tempCat = cbCategory != null ? cbCategory.getValue() : "All";
        final String category = tempCat != null ? tempCat : "All";
        
        if (!"All".equals(category)) {
            filtered = filtered.stream().filter(auction -> {
                if ("Art".equals(category)) return auction.getItem() instanceof Art;
                if ("Electronics".equals(category)) return auction.getItem() instanceof Electronics;
                if ("Vehicle".equals(category)) return auction.getItem() instanceof Vehicle;
                return false;
            }).collect(Collectors.toList());
        }

        filtered.sort(Comparator.comparing(Auction::getEndTime).reversed());
        renderAuctions(filtered);
    }
    //load các itemView vào trong mainpage
    private void renderAuctions(List<Auction> auctions) {
        // 1. Dọn dẹp các Observer cũ trước khi xóa các Node để giải phóng RAM
        for (Node node : gridPaneAuctions.getChildren()) {
            LotItemController controller = (LotItemController) node.getUserData();
            if (controller != null) controller.cleanup();
        }
        gridPaneAuctions.getChildren().clear();

        try {
            for (Auction auction : auctions) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/com/auction/client/view/itemView.fxml"));

                VBox itemNode = loader.load();

                LotItemController controller = loader.getController();
                controller.setData(auction);

                // Lưu tham chiếu controller vào Node để sau này có thể gọi hàm cleanup()
                itemNode.setUserData(controller);

                gridPaneAuctions.getChildren().add(itemNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleHomeAction(){
        initialize();
    }

    @FXML
    public void handleSellerViewTransfer(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/sellerView.fxml"));
            Parent sellerView = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Ép cửa sổ này nằm đè lên và chặn tương tác với cửa sổ cũ
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(sellerView));
            stage.setTitle("Inventory - Seller View");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddItemTransfer(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/addItem.fxml"));
            Parent addItemView = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Ép cửa sổ này nằm đè lên MainPage
            stage.setScene(new Scene(addItemView));
            stage.setTitle("Create New Auction");
            
            // Dùng showAndWait() để ứng dụng chờ bạn thao tác xong và đóng cửa sổ
            stage.showAndWait();
            initialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLogoutAction(ActionEvent actionEvent) {
        try {
            // Dọn dẹp Observer cho tất cả các LotItemController khi đăng xuất
            for (Node node : gridPaneAuctions.getChildren()) {
                LotItemController controller = (LotItemController) node.getUserData();
                if (controller != null) controller.cleanup();
            }

            // Xóa thông tin đăng nhập khi người dùng nhấn Logout
            ClientManager.getINSTANCE().clearUser();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/login.fxml"));
            Parent loginView = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cant logout");
        }
    }
}
