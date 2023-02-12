package com.amirali.wally.ui;

import com.amirali.wally.model.WallpaperItem;
import com.amirali.wally.ui.controllers.ImageViewerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class ImageViewerWindow extends Stage {

    public ImageViewerWindow(WallpaperItem wallpaperItem) {
        super(StageStyle.TRANSPARENT);
        setMaximized(true);

        var loader = new FXMLLoader(getClass().getResource("/com/amirali/wally/imageviewer-view.fxml"));
        try {
            setScene(new Scene(loader.load(), Color.TRANSPARENT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        var controller = ((ImageViewerController) loader.getController());

        controller.setWallpaperItem(wallpaperItem);
    }
}
