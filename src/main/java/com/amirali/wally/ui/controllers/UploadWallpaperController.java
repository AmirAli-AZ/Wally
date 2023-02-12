package com.amirali.wally.ui.controllers;

import com.amirali.wally.db.DBManager;
import com.amirali.wally.model.Categories;
import com.amirali.wally.model.WallpaperItem;
import com.amirali.wally.ui.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class UploadWallpaperController implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private TextField artistTextField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private Label categoryLabel;

    @FXML
    private TextArea description;

    @FXML
    private HBox hbox1;

    @FXML
    private HBox hbox2;

    @FXML
    private HBox hbox3;

    @FXML
    private Label thumbnailPathLabel;

    @FXML
    private Button thumbnailPickButton;

    @FXML
    private TextField titleTextField;

    @FXML
    private Button uploadButton;

    @FXML
    private Label wallpaperPathLabel;

    @FXML
    private Button wallpaperPickButton;

    private final ObjectProperty<File>
            wallpaper = new SimpleObjectProperty<>(),
            thumbnail = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleTextField.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));
        description.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));
        hbox1.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));
        hbox2.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));
        hbox3.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));
        artistTextField.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));

        categoryComboBox.getItems().addAll(Categories.list());
        categoryComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> categoryLabel.setText("Category: " + newValue));

        uploadButton.disableProperty().bind(
                titleTextField.textProperty().isEmpty().or(
                        description.textProperty().isEmpty().or(
                                artistTextField.textProperty().isEmpty().or(
                                        categoryComboBox.getSelectionModel().selectedItemProperty().isNull().or(
                                                wallpaper.isNull().or(
                                                        thumbnail.isNull()
                                                )
                                        )
                                )
                        )
                )
        );
    }

    @FXML
    void pickThumbnail(ActionEvent event) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Thumbnail");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPG Files", "*.jpg")
        );
        thumbnail.set(fileChooser.showOpenDialog(root.getScene().getWindow()));
        if (thumbnail.get() != null)
            thumbnailPathLabel.setText("Thumbnail (Max: 1MB): " + thumbnail.get().getAbsolutePath());
        else
            thumbnailPathLabel.setText("Thumbnail (Max: 1MB):");
    }

    @FXML
    void pickWallpaper(ActionEvent event) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Wallpaper");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("JPG Files", "*.jpg")
        );
        wallpaper.set(fileChooser.showOpenDialog(root.getScene().getWindow()));
        if (wallpaper.get() != null)
            wallpaperPathLabel.setText("Wallpaper (Max: 50MB): " + wallpaper.get().getAbsolutePath());
        else
            wallpaperPathLabel.setText("Wallpaper (Max: 50MB):");
    }

    @FXML
    void upload(ActionEvent event) {
        var progressDialog = new ProgressDialog("Please wait, uploading...");
        var thread = new Thread(() -> {
            Platform.runLater(progressDialog::show);

            var wallpaperItem = new WallpaperItem();
            wallpaperItem.setTitle(titleTextField.getText());
            wallpaperItem.setDescription(description.getText());
            wallpaperItem.setArtist(artistTextField.getText());
            wallpaperItem.setCategory(categoryComboBox.getSelectionModel().getSelectedItem());

            try {
                DBManager.getInstance().upload(wallpaperItem, wallpaper.get(), thumbnail.get(), percent -> Platform.runLater(() -> progressDialog.setProgress(percent/100)));
            } catch (Exception e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).show());
            }

            Platform.runLater(progressDialog::close);
        });
        thread.start();
    }

}
