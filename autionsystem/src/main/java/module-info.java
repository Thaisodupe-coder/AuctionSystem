module com.auction {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.auction to javafx.fxml;
    exports com.auction;
}
