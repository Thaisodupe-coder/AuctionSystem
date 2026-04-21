package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;

public class AddItemController {

    @FXML private TextField txtName;
    @FXML private ComboBox<String> cbCategory;
    @FXML private TextField txtStartPrice;
    @FXML private TextField txtIncrement;
    @FXML private TextArea txtDescription;
    @FXML private ImageView imgPreview;

    private File selectedImageFile;

    @FXML
    public void initialize() {
        cbCategory.getItems().addAll("Electronics", "Vehicles", "Real Estate", "Antiques");
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            selectedImageFile = file;
            Image image = new Image(file.toURI().toString());
            imgPreview.setImage(image);
        }
    }

    @FXML
    private void handleSubmit() {
        String name = txtName.getText();
        String price = txtStartPrice.getText();

        if (name.isEmpty() || price.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please fill in all required fields!");
            alert.show();
            return;
        }

        System.out.println("Creating auction for: " + name);

    }

    @FXML
    private void handleClear() {
        txtName.clear();
        txtStartPrice.clear();
        txtDescription.clear();
        imgPreview.setImage(null);
    }

    @FXML
    private void handleCancel(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/mainPage.fxml"));
            Parent mainView = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(mainView);
            stage.setScene(scene);
            stage.setTitle("Auction Client");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cancel Error");
        }
    }
}