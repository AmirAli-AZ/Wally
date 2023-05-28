package com.amirali.wally.ui.controllers;

import com.amirali.wally.App;
import com.amirali.wally.db.DBManager;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

public class SignInController implements Initializable {

    @FXML
    private PasswordField passwordField;

    @FXML
    private BorderPane root;

    @FXML
    private Button signInButton;

    @FXML
    private Hyperlink signUpLink;

    @FXML
    private TextField usernameTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usernameTextField.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));
        passwordField.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));

        signInButton.disableProperty().bind(
                usernameTextField.textProperty().isEmpty().
                        or(passwordField.textProperty().isEmpty()).
                        or(passwordField.textProperty().length().lessThan(4))
        );
    }

    @FXML
    void signIn(ActionEvent event) throws IOException, NoSuchAlgorithmException {
        var user = DBManager.getInstance().getUser(usernameTextField.getText());
        if (user.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Username doesn't exist", ButtonType.OK)
                    .showAndWait();
            return;
        }

        if (!user.get().getPassword().equals(DBManager.hashPassword(passwordField.getText()))) {
            new Alert(Alert.AlertType.ERROR, "Password is incorrect", ButtonType.OK)
                    .showAndWait();
            return;
        }

        DBManager.getInstance().writeUser(user.get());
        var window = ((Stage) root.getScene().getWindow());
        window.setScene(App.getInstance().getMainScene());
    }

    @FXML
    void signUp(ActionEvent event) throws IOException {
        var window = ((Stage) root.getScene().getWindow());
        window.setScene(App.getInstance().getSignUpScene());
    }
}
