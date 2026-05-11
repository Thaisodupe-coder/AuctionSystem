package com.auction.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
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

import java.io.File;
import java.time.LocalDate;

public class AddItemController {

    @FXML
    private ComboBox<String> cbCategory;

    @FXML
    private ComboBox<String> cbHour;

    @FXML
    private ComboBox<String> cbMinute;

    @FXML
    private DatePicker dpTimeEnd;

    @FXML
    private ImageView imgPreview;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtStartPrice;

    // Biến lưu trữ đường dẫn ảnh để sau này gửi lên server
    private String selectedImagePath;

    @FXML
    public void initialize() {
        // Thiết lập DayCellFactory để vô hiệu hóa các ngày trong quá khứ
        Callback<DatePicker, DateCell> dayCellFactory = dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #eeeeee;"); // Làm xám các ô không được chọn
                }
            }
        };
        dpTimeEnd.setDayCellFactory(dayCellFactory);

        // Khởi tạo giá trị cho Giờ (00 - 23)
        for (int i = 0; i < 24; i++) {
            cbHour.getItems().add(String.format("%02d", i));
        }
        // Khởi tạo giá trị cho Phút (bước nhảy 5 phút: 00, 05, 10, ... 55)
        for (int i = 0; i < 60; i += 5) {
            cbMinute.getItems().add(String.format("%02d", i));
        }
        cbHour.getSelectionModel().select("12"); // Mặc định chọn 12 giờ
        cbMinute.getSelectionModel().select("00"); // Mặc định chọn 00 phút
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
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
    }

}