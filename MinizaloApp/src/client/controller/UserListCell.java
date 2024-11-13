package client.controller;

import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import client.model.User;

public class UserListCell extends ListCell<User> {
    private HBox content;
    private Circle statusDot;
    private Label nameLabel;
    private Circle unreadIndicator;

    public UserListCell() {
        super();
        statusDot = new Circle(5);
        nameLabel = new Label();
        unreadIndicator = new Circle(4, Color.RED);
        unreadIndicator.setVisible(false);
        
        content = new HBox(10);
        content.getChildren().addAll(statusDot, nameLabel, unreadIndicator);
        content.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);
        if (empty || user == null) {
            setGraphic(null);
        } else {
            nameLabel.setText(user.getUsername());
            if (user.hasUnreadMessages()) {
                nameLabel.setStyle("-fx-font-weight: bold;");
            } else {
                nameLabel.setStyle("");
            }
            statusDot.setFill(user.isOnline() ? Color.GREEN : Color.GRAY);
            setGraphic(content);
        }
    }
}