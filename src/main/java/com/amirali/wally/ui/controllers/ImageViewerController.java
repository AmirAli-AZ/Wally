package com.amirali.wally.ui.controllers;

import com.amirali.wally.model.WallpaperItem;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class ImageViewerController implements Initializable {

    @FXML
    private Label artistLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private ImageView imageview;

    @FXML
    private Label publisherLabel;

    @FXML
    private StackPane root;

    @FXML
    private Label titleLabel;

    @FXML
    private HBox hbox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imageview.fitWidthProperty().bind(root.widthProperty());
        imageview.fitHeightProperty().bind(root.heightProperty());
        root.setOnMouseClicked(mouseEvent -> {
            if (hbox.isVisible()) {
                var animation = new TranslateTransition(Duration.millis(300), hbox);
                animation.setToY(hbox.getHeight());
                animation.setInterpolator(Interpolator.EASE_BOTH);
                animation.setOnFinished(actionEvent -> hbox.setVisible(false));
                animation.play();
            }else {
                hbox.setVisible(true);
                var animation = new TranslateTransition(Duration.millis(300), hbox);
                animation.setToY(0);
                animation.setInterpolator(Interpolator.EASE_BOTH);
                animation.play();
            }
        });
    }

    @FXML
    void close(ActionEvent event) {
        ((Stage) root.getScene().getWindow()).close();
    }

    public void setWallpaperItem(WallpaperItem wallpaperItem) {
        titleLabel.setText(wallpaperItem.getTitle());
        descriptionLabel.setText(wallpaperItem.getDescription());
        artistLabel.setText("Artist: " + wallpaperItem.getArtist());
        publisherLabel.setText("Publisher: " + wallpaperItem.getPublisher());
        imageview.setImage(wallpaperItem.getThumbnail());
    }
}
