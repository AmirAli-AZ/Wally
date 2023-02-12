package com.amirali.wally.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ProgressDialog extends Stage {

    private Label message;

    private ProgressBar progressBar;

    public ProgressDialog() {
        setScene(new Scene(createContent(), 400, 200));
    }

    public ProgressDialog(String message) {
        this();
        setMessage(message);
    }

    private Parent createContent() {
        message = new Label();
        message.setFont(Font.font(14));
        message.setMaxWidth(Double.MAX_VALUE);
        message.setAlignment(Pos.CENTER);

        progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);

        var root = new VBox(10, message, progressBar);
        root.setId("root");
        root.setPadding(new Insets(5));
        root.setAlignment(Pos.CENTER);

        return root;
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }

    public String getMessage() {
        return message.getText();
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public double getProgress() {
        return progressBar.getProgress();
    }
}
