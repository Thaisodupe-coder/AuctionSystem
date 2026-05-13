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

public class MainPageController {
    @FXML
    private FlowPane gridPaneAuctions;
    @FXML
    private Button txtusename;

    @FXML
    public void initialize() {
        String username = ClientManager.getINSTANCE().getUserName();
        String userId = ClientManager.getINSTANCE().getUserId();
        double balance = 100;//ClientManager.getINSTANCE().getCurrentUser().getBalance();
        int myAuctionCount = AuctionManager.getINSTANCE().getAuctionsBySeller(userId).size();

        if (username != null) {
            txtusename.setText("👤 " + username + "   |   💰 " + balance + "   |   📦 Lots: " + myAuctionCount);
        }

        List<Auction> auctions = AuctionManager.getINSTANCE().getAllAuctions();
        auctions.sort(Comparator.comparing(Auction::getEndTime).reversed());
        
        renderAuctions(auctions);
    }
    //load các itemView vào trong mainpage
    private void renderAuctions(List<Auction> auctions) {
        gridPaneAuctions.getChildren().clear();

        try {
            for (Auction auction : auctions) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/com/auction/client/view/itemView.fxml"));

                VBox itemNode = loader.load();

                LotItemController controller = loader.getController();
                controller.setData(auction);

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
