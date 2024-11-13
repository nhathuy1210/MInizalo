package client.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

public class Notification {
    private final VBox notificationBox;
    private final StackPane parentContainer;

    public Notification(StackPane parentContainer) {
        this.parentContainer = parentContainer;
        
        notificationBox = new VBox(5);
        notificationBox.setAlignment(Pos.TOP_LEFT);
        notificationBox.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #e0e0e0;");
        notificationBox.setMaxWidth(250);
        notificationBox.setMaxHeight(60);
        notificationBox.setOpacity(0);
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setRadius(4);
        notificationBox.setEffect(shadow);
        
        StackPane.setAlignment(notificationBox, Pos.TOP_RIGHT);
        StackPane.setMargin(notificationBox, new Insets(5, 5, 0, 0));
    }

    public void show(String title, String message) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        
        Label messageLabel = new Label(message.length() > 50 ? message.substring(0, 47) + "..." : message);
        messageLabel.setStyle("-fx-font-size: 11;");
        messageLabel.setWrapText(true);
        
        notificationBox.getChildren().clear();
        notificationBox.getChildren().addAll(titleLabel, messageLabel);
        
        if (!parentContainer.getChildren().contains(notificationBox)) {
            parentContainer.getChildren().add(notificationBox);
        }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), notificationBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(0.9);
        fadeIn.play();

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), notificationBox);
            fadeOut.setFromValue(0.9);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> parentContainer.getChildren().remove(notificationBox));
            fadeOut.play();
        });
        delay.play();
    }
}
