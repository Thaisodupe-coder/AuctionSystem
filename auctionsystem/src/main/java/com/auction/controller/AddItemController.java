package com.auction.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import com.auction.network.ClientManager;
import com.auction.network.message.Request;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddItemController {
    @FXML
    private TextField txtName;

    @FXML
    private TextField txtStartPrice;

    @FXML
    private ComboBox<String> cbCategory;

    @FXML
    private DatePicker dpEndDate;

    @FXML
    private ComboBox<String> cbEndHour;

    @FXML
    private ComboBox<String> cbEndMinute;

    @FXML
    private TextArea txtDescription;

    @FXML
    private ImageView imgPreview;


    // Biến lưu trữ đường dẫn ảnh để sau này gửi lên server
    private String selectedImagePath;

    @FXML
    public void initialize() {
        //thiết lập DayCellFactory để vô hiệu hóa các ngày trong quá khứ
        Callback<DatePicker, DateCell> dayCellFactory = dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #eeeeee;");
                }
            }
        };
        dpEndDate.setDayCellFactory(dayCellFactory);
        cbEndHour.getSelectionModel().select("12");
        cbEndMinute.getSelectionModel().select("00");
        //giá trị hours
        for (int i = 0; i < 24; i++) {
            cbEndHour.getItems().add(String.format("%02d", i));
        }
        //giá trị minute
        for (int i = 0; i < 60; i += 5) {
            cbEndMinute.getItems().add(String.format("%02d", i));
        }
        // Thêm các lựa chọn danh mục hiển thị trên giao diện (dropdown)
        cbCategory.getItems().addAll("Art", "Electronics","Vehicle");

        txtStartPrice.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtStartPrice.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        // Chỉ cho phép chọn các định dạng hình ảnh
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            Image image = new Image(selectedFile.toURI().toString());
            imgPreview.setImage(image);
        }
    }

    @FXML
    public void handleClear(ActionEvent event) {
        txtName.clear();
        txtStartPrice.clear();
        txtDescription.clear();
        cbCategory.getSelectionModel().clearSelection();
        dpEndDate.setValue(null);
        cbEndHour.getSelectionModel().select("12");
        cbEndMinute.getSelectionModel().select("00");
        imgPreview.setImage(null);
        selectedImagePath = null;
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        if (basicCheck()) {
            String name = txtName.getText();
            String startPrice = txtStartPrice.getText();
            String category = cbCategory.getValue();
            String description = txtDescription.getText();
            LocalDate endDate = dpEndDate.getValue();
            int endHour = Integer.parseInt(cbEndHour.getValue());
            int endMinute = Integer.parseInt(cbEndMinute.getValue());
            LocalDateTime endDateTime = endDate.atTime(endHour, endMinute);
            String sellerId = ClientManager.getINSTANCE().getUserId();

            // Đăng ký nhận kết quả trả về từ Server
            ClientManager.getINSTANCE().setResponseHandler(response -> {
                if ("CREATE_AUCTION_RES".equals(response.getCommand())) {
                    if ("SUCCESS".equals(response.getStatus())) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tạo phiên đấu giá thành công!");
                        // Đóng cửa sổ thêm sản phẩm sau khi tạo thành công
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.close();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Thất bại", response.getMessage());
                    }
                }
            });

            // Đóng gói Request và gửi đi
            Request request = new Request("CREATE_AUCTION");
            request.addData("sellerId", sellerId);
            request.addData("name", name);
            request.addData("startPrice", Double.parseDouble(startPrice));
            request.addData("category", category);
            request.addData("description", description);
            request.addData("endTime", endDateTime.toString());
            if (selectedImagePath != null) {
                request.addData("imagePath", selectedImagePath); // Tạm thời truyền đường dẫn để lưu
            }
            
            ClientManager.getINSTANCE().sendRequest(request);
        }
    }
    private boolean basicCheck(){
        String name = txtName.getText();
        String startPrice = txtStartPrice.getText();
        LocalDate endDate = dpEndDate.getValue();
        String category = cbCategory.getValue();
                if (endDate == null) {
            showAlert(Alert.AlertType.ERROR, "Thông báo lỗi", "Vui lòng chọn ngày kết thúc!");
            return false;
        }
        int endHour = Integer.parseInt(cbEndHour.getValue());
        int endMinute = Integer.parseInt(cbEndMinute.getValue());
        LocalDateTime endDateTime = endDate.atTime(endHour, endMinute);
        if (name == null || name.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Thông báo lỗi", "Tên sản phẩm không được để trống!");
            return false;
        }
        if (category == null) {
            showAlert(Alert.AlertType.ERROR, "Thông báo lỗi", "Vui lòng chọn danh mục sản phẩm!");
            return false;
        }
        if (startPrice == null || startPrice.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Thông báo lỗi", "Vui lòng điền giá khởi điểm!");
            return false;
        }

        if (endDateTime.isBefore(LocalDateTime.now())) {
            showAlert(Alert.AlertType.ERROR, "Thông báo lỗi", "Thời gian kết thúc phải lớn hơn thời gian hiện tại!");
            return false;
        }

        return true;
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
    }

}