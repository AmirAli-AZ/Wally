package com.amirali.wally.ui;

import com.amirali.wally.db.DBManager;
import com.amirali.wally.ui.CircularLoadingAnimationNode;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class SplashScreen extends Stage {

    private Runnable runnable;

    public SplashScreen() {
        super(StageStyle.TRANSPARENT);

        addEventHandler(WindowEvent.WINDOW_SHOWN, windowEvent -> {
            centerWindow();

            var thread = new Thread(() -> {
                DBManager.getInstance();
                runnable.run();
                Platform.runLater(this::close);
            });
            thread.start();
        });

        var root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to right, -fx-accent, #E05252);");
        root.setId("window");

        var appName = new Label("Wally");
        appName.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: white;");

        var description = new Label("Wally is a platform to share your wallpapers!");
        description.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        var center = new VBox(10, appName, description);
        center.setAlignment(Pos.CENTER);
        root.setCenter(center);

        var loadingAnimationNode = new CircularLoadingAnimationNode(Color.WHITE);
        loadingAnimationNode.play();
        var bottom = new HBox(loadingAnimationNode);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(new Insets(8));
        root.setBottom(bottom);

        setScene(new Scene(root, 600, 400, Color.TRANSPARENT));
    }

    private void centerWindow() {
        var bounds = Screen.getPrimary().getVisualBounds();
        setX(bounds.getMinX() + (bounds.getWidth() - getWidth()) / 2);
        setY(bounds.getMinY() + (bounds.getHeight() - getHeight()) / 2);
    }

    public void setOnConnected(Runnable runnable) {
        this.runnable = runnable;
    }
}