package com.amirali.wally.ui.controllers;

import com.amirali.wally.App;
import com.amirali.wally.db.DBManager;
import com.amirali.wally.model.User;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.ResourceBundle;

public class SignUpController implements Initializable {

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField nameTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private BorderPane root;

    @FXML
    private Hyperlink signInLink;

    @FXML
    private Button signUpButton;

    @FXML
    private TextField usernameTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usernameTextField.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));
        passwordField.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));
        emailTextField.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));
        nameTextField.prefWidthProperty().bind(root.widthProperty().multiply(85).divide(100));

        signUpButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () ->
                                usernameTextField.getText().isEmpty() ||
                                        passwordField.getText().isEmpty() || passwordField.getText().length() < 4 ||
                                        emailTextField.getText().isEmpty() ||
                                        nameTextField.getText().isEmpty() ||
                                        !isValidEmail(emailTextField.getText()),
                        usernameTextField.textProperty(),
                        passwordField.textProperty(),
                        passwordField.textProperty().length(),
                        emailTextField.textProperty(),
                        nameTextField.textProperty()
                )
        );
    }

    @FXML
    void signUp(ActionEvent event) throws IOException, NoSuchAlgorithmException {
        var user = new User(
                usernameTextField.getText(),
                DBManager.hashPassword(passwordField.getText()),
                nameTextField.getText(),
                emailTextField.getText(),
                List.of()
        );

        var success = DBManager.getInstance().createUser(user);
        if (!success) {
            new Alert(Alert.AlertType.ERROR, "Username exists", ButtonType.OK)
                    .showAndWait();
        }else {
            DBManager.getInstance().writeUser(user);
            var window = ((Stage) root.getScene().getWindow());
            window.setScene(App.getInstance().getMainScene());
        }
    }

    @FXML
    void signIn(ActionEvent actionEvent) throws IOException {
        var window = ((Stage) root.getScene().getWindow());
        window.setScene(App.getInstance().getSingInScene());
    }

    private boolean isValidEmail(String email) {
        return email.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
    }
}
