package com.amirali.wally.ui;

import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class CircularLoadingAnimationNode extends HBox {

    private final ObjectProperty<Duration> durationProperty = new SimpleObjectProperty<>();

    public CircularLoadingAnimationNode(double radius, Color color, Duration duration) {
        setAlignment(Pos.CENTER);
        durationProperty.set(duration);
        for (int i = 0; i < 6; i++) {
            var circle = new Circle(radius, color);
            circle.setOpacity(0.3);
            getChildren().add(circle);
        }
        setSpacing(3);
        play();
    }

    public CircularLoadingAnimationNode(Color color, Duration duration) {
        this(10, color, duration);
    }

    public CircularLoadingAnimationNode(Color color) {
        this(color, Duration.millis(600));
    }

    public CircularLoadingAnimationNode() {
        this(Color.WHITE, Duration.millis(600));
    }

    private void play() {
        var t = new Thread(() -> {
            for (int i = 0; i < getChildren().size(); i++) {
                var animation = new FadeTransition(durationProperty.get(), getChildren().get(i));
                animation.setFromValue(0.3);
                animation.setToValue(1);
                animation.setOnFinished(actionEvent -> animation.playFromStart());
                animation.setAutoReverse(true);
                animation.setCycleCount(2);
                animation.play();
                try {
                    Thread.sleep((long) (durationProperty.get().toMillis()/(getChildren().size() - 1)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
