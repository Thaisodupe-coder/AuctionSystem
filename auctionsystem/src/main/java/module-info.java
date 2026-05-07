module com.auction {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    exports com.auction;
    exports com.auction.controller;
    exports com.auction.server;
    exports com.auction.model.user;
    exports com.auction.model.item;
    exports com.auction.model.auction;
    exports com.auction.model.common;
    exports com.auction.service;

    opens com.auction to javafx.fxml, org.junit.platform.commons;
    opens com.auction.controller to javafx.fxml;
    opens com.auction.network.message to com.google.gson;
}
