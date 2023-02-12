package com.amirali.wally.ui;

import com.amirali.wally.db.DBManager;
import com.amirali.wally.model.WallpaperItem;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ListItem extends ListCell<WallpaperItem> {

    private VBox root;

    private ImageView thumbnail;

    private Label title, publisher;

    private Button downloadButton, deleteButton;


    @Override
    protected void updateItem(WallpaperItem item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);

        if (item == null || empty) {
            setGraphic(null);
        }else {
            if (root == null)
                createContent();
            setInformation(item);
            setGraphic(root);
        }
    }

    private void createContent() {
        thumbnail = new ImageView();
        thumbnail.setPreserveRatio(true);
        thumbnail.fitWidthProperty().bind(widthProperty().subtract(30));
        title = new Label();
        title.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 18));
        publisher = new Label();

        var vbox = new VBox(3, title, publisher);
        vbox.setPadding(new Insets(10));
        HBox.setHgrow(vbox, Priority.ALWAYS);

        downloadButton = new Button("Download");
        downloadButton.setId("download-button");
        downloadButton.getStyleClass().add("icon-button");
        downloadButton.setOnAction(actionEvent -> {
            var savedUser = DBManager.getInstance().readUser();
            if (savedUser.isEmpty() || !savedUser.get().getUsername().equals(getItem().getPublisher()))
                return;

            var progressDialog = new ProgressDialog("Please wait, downloading...");
            var thread = new Thread(() -> {
                Platform.runLater(progressDialog::show);

                var item = getItem();

                try {
                    var downloadFolder = Paths.get(System.getProperty("user.home") + File.separator + "Downloads");
                    Files.createDirectories(downloadFolder);

                    var destPath = Paths.get(downloadFolder + File.separator + item.getFilename());
                    var number = 1;
                    while (Files.exists(destPath)) {
                        var name = item.getFilename().substring(0, item.getFilename().lastIndexOf('.'));
                        var extension = item.getFilename().substring(item.getFilename().lastIndexOf('.'));
                        destPath = Paths.get(downloadFolder + File.separator + name + "-" + number + extension);
                        number++;
                    }

                    DBManager.getInstance().download(item, destPath.toFile(), percent -> Platform.runLater(() -> progressDialog.setProgress(percent)));
                } catch (Exception e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).show());
                }

                Platform.runLater(progressDialog::close);
            });
            thread.start();
        });
        downloadButton.setCursor(Cursor.HAND);
        downloadButton.setGraphic(new FontIcon(FontAwesome.DOWNLOAD));

        deleteButton = new Button();
        deleteButton.setId("delete-button");
        deleteButton.getStyleClass().add("icon-button");
        deleteButton.setOnAction(actionEvent -> {
            var savedUser = DBManager.getInstance().readUser();
            if (savedUser.isEmpty() || !savedUser.get().getUsername().equals(getItem().getPublisher()))
                return;

            var thread = new Thread(() -> {
                try {
                    DBManager.getInstance().remove(getItem());
                    Platform.runLater(() -> getListView().getItems().remove(getItem()));
                }catch (Exception e) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).show());
                }
            });
            thread.start();
        });
        deleteButton.setCursor(Cursor.HAND);
        deleteButton.setGraphic(new FontIcon(FontAwesome.TRASH));

        var hbox = new HBox(3, vbox, downloadButton, deleteButton);
        hbox.setPadding(new Insets(5));
        hbox.setAlignment(Pos.CENTER_LEFT);

        root = new VBox(thumbnail, hbox);
        root.setAlignment(Pos.CENTER);
        root.setId("cell-root");

        root.setOnMouseClicked(mouseEvent -> {
            var imageViewer = new ImageViewerWindow(getItem());
            imageViewer.show();
        });
    }

    private void setInformation(WallpaperItem item) {
        thumbnail.setImage(item.getThumbnail());
        title.setText(item.getTitle());
        publisher.setText("Publisher: " + item.getPublisher());
        var savedUser = DBManager.getInstance().readUser();
        if (savedUser.isEmpty() || !savedUser.get().getUsername().equals(getItem().getPublisher())) {
            deleteButton.setVisible(false);
            downloadButton.setVisible(false);
        }else if (savedUser.get().getUsername().equals(getItem().getPublisher())){
            deleteButton.setVisible(true);
            downloadButton.setVisible(true);
        }
    }
}
