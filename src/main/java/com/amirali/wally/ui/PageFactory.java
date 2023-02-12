package com.amirali.wally.ui;

import com.amirali.wally.db.DBManager;
import com.amirali.wally.model.WallpaperItem;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.bson.conversions.Bson;

public class PageFactory implements Callback<Integer, Node> {

    private StackPane root;

    private ListView<WallpaperItem> listview;

    private CircularLoadingAnimationNode circularLoading;

    private Bson filter;

    @Override
    public Node call(Integer param) {
        if (root == null) {
            listview = new ListView<>();
            listview.setCellFactory(param1 -> new ListItem());

            circularLoading = new CircularLoadingAnimationNode();
            circularLoading.setId("wallpapers-loading");

            root = new StackPane();
        }

        var thread = new Thread(() -> {
            Platform.runLater(() -> {
                listview.getItems().clear();
                root.getChildren().clear();
                root.getChildren().add(circularLoading);
            });
            var index = param * 10;
            var wallpapers = DBManager.getInstance().getWallpapers(filter, index, 10);
            Platform.runLater(() -> {
                listview.getItems().addAll(wallpapers);
                root.getChildren().clear();
                root.getChildren().add(listview);
            });
            setFilter(null);
        });
        thread.setDaemon(true);
        thread.start();

        return root;
    }

    public void setFilter(Bson filter) {
        this.filter = filter;
    }
}
