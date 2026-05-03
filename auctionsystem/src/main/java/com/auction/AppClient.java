package com.auction;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppClient extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader =  new FXMLLoader(getClass().getResource("/com/auction/client/view/login.fxml"));

        Parent root = loader.load();

        Scene scene = new Scene(root);

        primaryStage.setTitle("Auction System");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Application stopped");
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

