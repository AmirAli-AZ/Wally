package com.amirali.wally.ui;

import com.amirali.wally.db.DBManager;
import com.amirali.wally.model.WallpaperItem;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.bson.conversions.Bson;


public class PageFactory implements Callback<Integer, Node> {

    private ListView<WallpaperItem> listview;

    private Bson filter;

    @Override
    public Node call(Integer param) {
        if (listview == null) {
            listview = new ListView<>();
            listview.setCellFactory(param1 -> new ListItem());
        }

        var thread = new Thread(() -> {
            Platform.runLater(() -> listview.getItems().clear());
            var index = param * 10;
            var wallpapers = DBManager.getInstance().getWallpapers(filter, index, 10);
            Platform.runLater(() -> listview.getItems().addAll(wallpapers));
            setFilter(null);
        });
        thread.setDaemon(true);
        thread.start();

        return listview;
    }

    public void setFilter(Bson filter) {
        this.filter = filter;
    }
}
