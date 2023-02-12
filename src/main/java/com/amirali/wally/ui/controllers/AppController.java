package com.amirali.wally.ui.controllers;

import com.amirali.wally.App;
import com.amirali.wally.db.DBManager;
import com.amirali.wally.ui.PageFactory;
import com.amirali.wally.utils.Theme;
import com.amirali.wally.utils.ThemeManager;
import com.mongodb.client.model.Filters;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class AppController implements Initializable {

    @FXML
    private VBox topPane;

    @FXML
    private TextField searchField;

    @FXML
    private BorderPane root;

    @FXML
    private Pagination pagination;

    @FXML
    private CheckMenuItem darkThemeCheckMenuItem;

    private ContextMenu contextMenu;

    private ToggleGroup radioItemsToggleGroup;

    private PageFactory pageFactory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int pageCount = (int) Math.ceil(DBManager.getInstance().getCollection("wallpapers.files").countDocuments()/10.0);
        pagination.setPageCount(pageCount);
        pageFactory = new PageFactory();
        pagination.setPageFactory(pageFactory);
        darkThemeCheckMenuItem.setSelected(ThemeManager.load() == Theme.DARK);
        searchField.prefWidthProperty().bind(topPane.widthProperty().multiply(85).divide(100));
        contextMenu = createSearchFilterMenu();
    }

    @FXML
    void close(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    void upload(ActionEvent actionEvent) throws IOException {
        var stage = new Stage();
        stage.setTitle("Wally-Upload New Wallpaper");
        stage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/amirali/wally/upload-wallpaper-view.fxml")))));
        stage.show();
    }

    @FXML
    void signin(ActionEvent actionEvent) throws IOException {
        var stage = ((Stage) root.getScene().getWindow());
        stage.setScene(App.getInstance().getSingInScene());
    }

    @FXML
    void refresh(ActionEvent actionEvent) {
        int pageCount = (int) Math.ceil(DBManager.getInstance().getCollection("wallpapers.files").countDocuments()/10.0);
        pagination.setPageCount(pageCount);

        pagination.getPageFactory().call(pagination.getCurrentPageIndex());
    }

    public void darkTheme(ActionEvent actionEvent) throws IOException {
        var theme = darkThemeCheckMenuItem.isSelected() ? Theme.DARK : Theme.LIGHT;
        ThemeManager.applyThemeToAllWindows(theme);
        ThemeManager.save(theme);
    }

    @FXML
    void searchFilter(ActionEvent actionEvent) {
        if (!contextMenu.isShowing())
            contextMenu.show((Node) actionEvent.getSource(), Side.BOTTOM, 0, 0);
    }

    private ContextMenu createSearchFilterMenu() {
        var titleItem = new RadioMenuItem("Title");
        titleItem.setUserData("metadata.title");
        var descriptionItem = new RadioMenuItem("Description");
        descriptionItem.setUserData("metadata.description");
        var artistItem = new RadioMenuItem("Artist");
        artistItem.setUserData("metadata.artist");
        var publisherItem = new RadioMenuItem("Publisher");
        publisherItem.setUserData("metadata.publisher");
        var categoryItem = new RadioMenuItem("Category");
        categoryItem.setUserData("metadata.category");

        radioItemsToggleGroup = new ToggleGroup();
        radioItemsToggleGroup.getToggles().addAll(titleItem, descriptionItem, artistItem, publisherItem, categoryItem);
        radioItemsToggleGroup.selectToggle(titleItem);

        return new ContextMenu(titleItem, descriptionItem, artistItem, publisherItem, categoryItem);
    }

    @FXML
    void search(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            int pageCount = (int) Math.ceil(DBManager.getInstance().getCollection("wallpapers.files").countDocuments()/10.0);
            pagination.setPageCount(pageCount);
            var filter = Filters.eq(
                    (String) radioItemsToggleGroup.getSelectedToggle().getUserData(),
                    Pattern.compile(".*" + searchField.getText().trim() + ".*" , Pattern.CASE_INSENSITIVE)
            );
            pageFactory.setFilter(filter);
            pagination.getPageFactory().call(pagination.getCurrentPageIndex());
        }
    }
}
