package com.amirali.wally;

import com.amirali.wally.db.DBManager;
import com.amirali.wally.ui.SplashScreen;
import com.amirali.wally.utils.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    private static App instance;

    public App() {
        instance = this;
    }

    public static App getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Wally");
        syncThemesAndIcons();
        ThemeManager.save(ThemeManager.load());

        var splashScreen = new SplashScreen();
        splashScreen.setOnConnected(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                try {
                    var savedUser = DBManager.getInstance().readUser();
                    if (savedUser.isEmpty()) {
                        primaryStage.setScene(getSingInScene());
                    }else {
                        var user = DBManager.getInstance().getUser(savedUser.get().getUsername());
                        if (user.isEmpty()) {
                            primaryStage.setScene(getSignUpScene());
                            return;
                        }

                        if (user.get().getPassword().equals(savedUser.get().getPassword()))
                            primaryStage.setScene(getMainScene());
                        else
                            primaryStage.setScene(getSignUpScene());
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }
                primaryStage.show();
            });
        });
        splashScreen.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void syncThemesAndIcons() {
        Window.getWindows().addListener((ListChangeListener<? super Window>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Window window : change.getAddedSubList()) {
                        if (window instanceof Stage stage) {
                            ThemeManager.setTheme(stage.getScene(), ThemeManager.load());
                            stage.sceneProperty().addListener((observableValue, scene, newScene) -> ThemeManager.setTheme(newScene, ThemeManager.load()));
                        }
                    }
                }
            }
        });
    }

    public Scene getSingInScene() throws IOException {
        return new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("signin-view.fxml"))));
    }

    public Scene getSignUpScene() throws IOException {
        return new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("signup-view.fxml"))));
    }

    public Scene getMainScene() throws IOException {
        return new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("app-view.fxml"))));
    }
}
