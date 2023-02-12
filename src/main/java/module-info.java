module com.amirali.wally {
    requires javafx.controls;
    requires javafx.fxml;
    requires mongo.java.driver;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome;

    opens com.amirali.wally.ui.controllers to javafx.fxml;
    exports com.amirali.wally;
    exports com.amirali.wally.ui;
    exports com.amirali.wally.model;
}